package com.mashro3ai.order.application;
import com.mashro3ai.ai.application.OrderExtractionPort;
import com.mashro3ai.ai.model.ExtractionContext;
import com.mashro3ai.business.CurrentBusinessProvider;
import com.mashro3ai.order.api.dto.*;
import com.mashro3ai.order.domain.DeliveryMethod;
import jakarta.validation.Validator;
import java.time.*;
import java.util.Currency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile("local")
public class DemoExtractionService {
    private final OrderExtractionPort parser;
    private final OrderDraftService drafts;
    private final CurrentBusinessProvider business;
    private final Validator validator;
    private final Currency currency;
    private final ZoneId timezone;
    public DemoExtractionService(OrderExtractionPort parser,OrderDraftService drafts,CurrentBusinessProvider business,Validator validator,
        @Value("${mashro3.demo.currency}") String currency,@Value("${mashro3.demo.timezone}") String timezone) {
        this.parser=parser; this.drafts=drafts; this.business=business; this.validator=validator;
        this.currency=Currency.getInstance(currency); this.timezone=ZoneId.of(timezone);
    }
    public OrderDraftResponse extract(ExtractOrderDraftRequest r) {
        CreateOrderDraftRequest candidate;
        try {
            var result=parser.extract(r.message(),new ExtractionContext(business.currentBusinessId(),r.referenceTimestamp()==null?Instant.now():r.referenceTimestamp(),timezone,currency));
            var c=result.customer(); var d=result.delivery();
            candidate=new CreateOrderDraftRequest(new CustomerDetailsDto(c.name(),c.phone(),c.email()),
                new DeliveryDetailsDto(d.method()==null?null:DeliveryMethod.valueOf(d.method()),d.area(),d.address(),d.requestedDate(),d.originalDateText(),d.notes()),
                result.items().stream().map(i->new OrderItemDraftDto(null,i.description(),i.quantity(),i.unitPrice(),i.notes())).toList(),currency.getCurrencyCode(),null);
        } catch(IllegalArgumentException | java.time.format.DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use the labelled demo format: ISO date, DELIVERY/PICKUP, and item: description | quantity | price");
        }
        if(!validator.validate(candidate).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Parsed fields failed validation; check field lengths, email, quantity and price");
        return drafts.create(candidate,r.message());
    }
}
