package com.mashro3ai.order.application;

import com.mashro3ai.ai.model.*;
import com.mashro3ai.ai.provider.*;
import com.mashro3ai.business.CurrentBusinessProvider;
import com.mashro3ai.catalog.CatalogService;
import com.mashro3ai.order.api.dto.*;
import com.mashro3ai.order.domain.DeliveryMethod;
import jakarta.validation.Validator;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Coordinates extraction, catalogue grounding, validation and durable draft creation. */
@Service
@Profile({"local", "demo"})
public class DemoExtractionService {
    private final LocalDemoOrderParser parser;
    private final OllamaOrderExtractor ollama;
    private final CatalogService catalog;
    private final OrderDraftService drafts;
    private final CurrentBusinessProvider business;
    private final Validator validator;
    private final Currency currency;
    private final ZoneId timezone;

    public DemoExtractionService(LocalDemoOrderParser parser, OllamaOrderExtractor ollama,
            CatalogService catalog, OrderDraftService drafts, CurrentBusinessProvider business, Validator validator,
            @Value("${mashro3.demo.currency}") String currency, @Value("${mashro3.demo.timezone}") String timezone) {
        this.parser = parser; this.ollama = ollama; this.catalog = catalog; this.drafts = drafts;
        this.business = business; this.validator = validator;
        this.currency = Currency.getInstance(currency); this.timezone = ZoneId.of(timezone);
    }

    public OrderDraftResponse extract(ExtractOrderDraftRequest request) {
        boolean useModel = "ollama".equals(request.engine());
        CreateOrderDraftRequest candidate;
        var warnings = new ArrayList<ExtractionWarningDto>();
        try {
            var context = new ExtractionContext(business.currentBusinessId(),
                request.referenceTimestamp() == null ? Instant.now() : request.referenceTimestamp(), timezone, currency);
            var result = (useModel ? ollama : parser).extract(request.message(), context);
            result.warnings().forEach(w -> warnings.add(new ExtractionWarningDto(w.code(), w.path(), w.message())));
            var items = new ArrayList<OrderItemDraftDto>();
            for (var item : result.items()) {
                var product = useModel ? catalog.find(item.sku(), item.description()) : Optional.<CatalogService.Product>empty();
                if (useModel && product.isEmpty()) warnings.add(new ExtractionWarningDto("UNKNOWN_PRODUCT", "items[" + items.size() + "]", "No catalogue match. Review the description and enter the price."));
                items.add(new OrderItemDraftDto(product.map(CatalogService.Product::id).orElse(null),
                    product.map(CatalogService.Product::name).orElse(item.description()), item.quantity(),
                    useModel ? product.map(CatalogService.Product::price).orElse(null) : item.unitPrice(), item.notes()));
            }
            var c = result.customer(); var d = result.delivery();
            candidate = new CreateOrderDraftRequest(new CustomerDetailsDto(c.name(), c.phone(), c.email()),
                new DeliveryDetailsDto(d.method() == null ? null : DeliveryMethod.valueOf(d.method()),
                    d.area(), d.address(), d.requestedDate(), d.originalDateText(), d.notes()), items, currency.getCurrencyCode(), null);
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException ex) {
            throw new ResponseStatusException(useModel ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST,
                useModel ? "The model returned invalid values. Nothing was saved; please retry or enter manually."
                    : "Use labelled lines, an ISO date, DELIVERY/PICKUP, and item: description | quantity | price");
        }
        if (!validator.validate(candidate).isEmpty()) throw new ResponseStatusException(
            useModel ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST,
            "Extracted fields failed validation. Nothing was saved; check field lengths, email, quantity and price.");
        return drafts.create(candidate, request.message(), warnings);
    }
}
