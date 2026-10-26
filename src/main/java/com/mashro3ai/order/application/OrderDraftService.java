package com.mashro3ai.order.application;

import com.mashro3ai.business.CurrentBusinessProvider;
import com.mashro3ai.order.api.dto.*;
import com.mashro3ai.order.domain.*;
import com.mashro3ai.order.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class OrderDraftService {
    private final OrderDraftRepository drafts;
    private final ConfirmedOrderRepository orders;
    private final CurrentBusinessProvider business;
    public OrderDraftService(OrderDraftRepository drafts, ConfirmedOrderRepository orders, CurrentBusinessProvider business) {
        this.drafts=drafts; this.orders=orders; this.business=business;
    }
    public OrderDraftResponse create(CreateOrderDraftRequest r) { return create(r, null); }
    public OrderDraftResponse create(CreateOrderDraftRequest r, String source) {
        return create(r, source, List.of());
    }
    public OrderDraftResponse create(CreateOrderDraftRequest r, String source, List<ExtractionWarningDto> warnings) {
        var d=new OrderDraft(business.currentBusinessId(),r.currency(),source,customer(r.customer()),delivery(r.delivery()),r.notes());
        d.setExtractionWarnings(new tools.jackson.databind.json.JsonMapper().writeValueAsString(warnings));
        addItems(d,r.items()); drafts.saveAndFlush(d); return response(d);
    }
    @Transactional(readOnly=true)
    public OrderDraftResponse get(UUID id) { return response(find(id)); }
    public OrderDraftResponse update(UUID id, SaveOrderDraftRequest r) {
        var d=find(id);
        if(d.getStatus()!=DraftStatus.DRAFT) throw new ResponseStatusException(HttpStatus.CONFLICT,"Confirmed drafts cannot be edited");
        checkVersion(d,r.version());
        d.replace(customer(r.customer()),delivery(r.delivery()),r.currency(),r.notes());
        addItems(d,r.items()); drafts.flush(); return response(d);
    }
    public Confirmation confirm(UUID id, ConfirmOrderRequest r) {
        var d=find(id);
        if(d.getStatus()==DraftStatus.CONFIRMED) return new Confirmation(order(d.getConfirmedOrderId()),false);
        checkVersion(d,r.expectedVersion());
        var missing=missing(d);
        if(!missing.isEmpty()) {
            var problem=ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,"Complete the draft before confirming");
            problem.setProperty("missingFields",missing);
            throw new org.springframework.web.ErrorResponseException(HttpStatus.UNPROCESSABLE_ENTITY,problem,null);
        }
        var o=orders.saveAndFlush(new ConfirmedOrder(d));
        d.confirm(o.getId()); drafts.flush(); return new Confirmation(orderResponse(o,d),true);
    }
    @Transactional(readOnly=true)
    public OrderResponse order(UUID id) {
        var o=orders.findByIdAndBusinessId(id,business.currentBusinessId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
        return orderResponse(o,find(o.getDraftId()));
    }
    @Transactional(readOnly=true)
    public OrderPageResponse list(int page,int size) {
        if(page<0 || size<1 || size>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"page must be >= 0 and size must be 1–100");
        var result=orders.findByBusinessId(business.currentBusinessId(),PageRequest.of(page,size,Sort.by(Sort.Order.desc("createdAt"),Sort.Order.desc("id"))));
        return new OrderPageResponse(result.stream().map(o->orderResponse(o,find(o.getDraftId()))).toList(),page,size,result.getTotalElements(),result.getTotalPages());
    }
    @Transactional(readOnly=true)
    public ClarificationResponse clarification(UUID id) {
        var fields=missing(find(id));
        return new ClarificationResponse(id,fields.isEmpty()?"Thank you. Your order details are complete.":"Please provide: "+String.join(", ",fields.stream().map(MissingFieldDto::path).toList())+".",fields.stream().map(MissingFieldDto::path).toList());
    }
    @Transactional(readOnly=true)
    public DraftPage listDrafts(int page, int size) {
        if(page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"page must be >= 0 and size must be 1–100");
        var result = drafts.findByBusinessId(business.currentBusinessId(), PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return new DraftPage(result.stream().map(OrderDraftService::response).toList(), page, size, result.getTotalElements(), result.getTotalPages());
    }
    public record DraftPage(List<OrderDraftResponse> content, int page, int size, long totalElements, int totalPages) {}

    @Transactional(readOnly=true)
    public ClarificationResponse clarification(UUID id, String language) {
        var fields = missing(find(id));
        boolean arabic = "ar".equals(language);
        var labels = Map.ofEntries(
            Map.entry("customer.name", arabic ? "اسم العميل" : "your name"),
            Map.entry("customer.phone", arabic ? "رقم الهاتف" : "your phone number"),
            Map.entry("delivery.method", arabic ? "التوصيل أم الاستلام" : "delivery or pickup"),
            Map.entry("delivery.area", arabic ? "منطقة التوصيل" : "delivery area"),
            Map.entry("delivery.address", arabic ? "عنوان التوصيل بالتفصيل" : "full delivery address"),
            Map.entry("delivery.requestedDate", arabic ? "تاريخ التوصيل أو الاستلام" : "delivery or pickup date"),
            Map.entry("items", arabic ? "المنتجات المطلوبة" : "the products you would like"));
        // Prices are owner-only fields; never ask the customer to supply catalogue prices.
        var customerFields = fields.stream().filter(f -> !f.path().endsWith("unitPrice")).toList();
        String text = customerFields.isEmpty()
            ? (arabic ? "شكراً! سنراجع طلبك ونؤكد التفاصيل قريباً." : "Thank you! We will review your order and confirm the details shortly.")
            : (arabic ? "أهلاً! لإكمال طلبك، ممكن ترسل لنا: " : "Hi! To complete your order, please send: ")
                + String.join(arabic ? "، " : ", ", customerFields.stream().map(f -> labels.getOrDefault(f.path(),
                    f.path().endsWith("quantity") ? (arabic ? "الكمية المطلوبة للصنف " : "quantity for item ") + (itemIndex(f.path()) + 1)
                    : (arabic ? "وصف الصنف " : "description for item ") + (itemIndex(f.path()) + 1))).toList()) + ".";
        return new ClarificationResponse(id, text, customerFields.stream().map(MissingFieldDto::path).toList());
    }
    private static int itemIndex(String path) {
        var matcher = java.util.regex.Pattern.compile("items\\[(\\d+)\\]").matcher(path);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
    private OrderDraft find(UUID id) {
        return drafts.findByIdAndBusinessId(id,business.currentBusinessId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Draft not found"));
    }
    private void checkVersion(OrderDraft d,long expected) {
        if(d.getVersion()!=expected) throw new ResponseStatusException(HttpStatus.CONFLICT,"Draft changed; reload it before saving");
    }
    private static DraftCustomer customer(CustomerDetailsDto c) { return c==null?null:new DraftCustomer(c.name(),c.phone(),c.email()); }
    private static DraftDelivery delivery(DeliveryDetailsDto d) { return d==null?null:new DraftDelivery(d.method(),d.area(),d.address(),d.requestedDate(),d.originalDateText(),d.notes()); }
    private static void addItems(OrderDraft d,List<OrderItemDraftDto> items) { items.forEach(i->d.addItem(i.productId(),i.description(),i.quantity(),i.unitPrice(),i.notes())); }
    private static boolean blank(String s) { return s==null || s.isBlank(); }
    private static List<MissingFieldDto> missing(OrderDraft d) {
        var m=new ArrayList<MissingFieldDto>();
        if(d.getCustomer()==null || blank(d.getCustomer().getName())) m.add(new MissingFieldDto("customer.name","Customer name is required"));
        if(d.getCustomer()==null || blank(d.getCustomer().getPhone())) m.add(new MissingFieldDto("customer.phone","Customer phone is required"));
        var delivery=d.getDelivery();
        if(delivery==null || delivery.getMethod()==null) m.add(new MissingFieldDto("delivery.method","Choose delivery or pickup"));
        if(delivery!=null && delivery.getMethod()==DeliveryMethod.DELIVERY) {
            if(blank(delivery.getArea())) m.add(new MissingFieldDto("delivery.area","Delivery area is required"));
            if(blank(delivery.getAddress())) m.add(new MissingFieldDto("delivery.address","Delivery address is required"));
        }
        if(delivery==null || delivery.getRequestedDate()==null) m.add(new MissingFieldDto("delivery.requestedDate","Confirm a delivery or pickup date"));
        if(d.getItems().isEmpty()) m.add(new MissingFieldDto("items","Add at least one item"));
        for(int n=0;n<d.getItems().size();n++) {
            var i=d.getItems().get(n); var prefix="items["+n+"].";
            if(blank(i.getDescription())) m.add(new MissingFieldDto(prefix+"description","Item description is required"));
            if(i.getQuantity()==null) m.add(new MissingFieldDto(prefix+"quantity","Quantity is required"));
            if(i.getUnitPrice()==null) m.add(new MissingFieldDto(prefix+"unitPrice","Unit price is required"));
        }
        return List.copyOf(m);
    }
    private static OrderDraftResponse response(OrderDraft d) {
        var c=d.getCustomer(); var v=d.getDelivery();
        var warnings=d.getExtractionWarnings()==null
            ? (d.getSourceMessage()==null?List.<ExtractionWarningDto>of():List.of(new ExtractionWarningDto("DEMO_PARSER","sourceMessage","Legacy demo parser. Review every field.")))
            : List.of(new tools.jackson.databind.json.JsonMapper().readValue(d.getExtractionWarnings(), ExtractionWarningDto[].class));
        return new OrderDraftResponse(d.getId(),d.getBusinessId(),d.getVersion(),d.getStatus(),d.getSourceMessage(),
            c==null?null:new CustomerDetailsDto(c.getName(),c.getPhone(),c.getEmail()),
            v==null?null:new DeliveryDetailsDto(v.getMethod(),v.getArea(),v.getAddress(),v.getRequestedDate(),v.getOriginalDateText(),v.getNotes()),
            d.getItems().stream().map(i->new OrderItemDraftDto(i.getProductId(),i.getDescription(),i.getQuantity(),i.getUnitPrice(),i.getNotes())).toList(),
            d.getCurrency(),d.getNotes(),missing(d),warnings,d.getConfirmedOrderId(),d.getCreatedAt(),d.getUpdatedAt());
    }
    private static OrderResponse orderResponse(ConfirmedOrder o,OrderDraft d) {
        var r=response(d);
        var items=d.getItems().stream().map(i->new OrderItemResponse(i.getProductId(),i.getDescription(),i.getQuantity(),i.getUnitPrice(),i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())),i.getNotes())).toList();
        var total=items.stream().map(OrderItemResponse::lineTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        return new OrderResponse(o.getId(),o.getBusinessId(),d.getId(),0,OrderStatus.CONFIRMED,r.customer(),r.delivery(),items,d.getCurrency(),total,d.getNotes(),o.getCreatedAt(),o.getCreatedAt());
    }
    public record Confirmation(OrderResponse order,boolean created) {}
}
