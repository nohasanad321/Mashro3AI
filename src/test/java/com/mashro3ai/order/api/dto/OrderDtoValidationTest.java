package com.mashro3ai.order.api.dto;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderDtoValidationTest {
    private <T> long violations(T value) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(value).size();
        }
    }
    @Test void extractionRequiresMessage() {
        assertTrue(violations(new ExtractOrderDraftRequest("  ", null)) > 0);
    }
    @Test void incompleteDraftIsAllowed() {
        assertEquals(0, violations(new CreateOrderDraftRequest(null, null,
            List.of(new OrderItemDraftDto(null, "Cookie box", null, null, null)), "KWD", null)));
    }
    @Test void validatesNestedItemsAndThreeDecimalMoney() {
        assertEquals(0, violations(new OrderItemDraftDto(null, "Cookie box", 2, new BigDecimal("1.250"), null)));
        assertTrue(violations(new CreateOrderDraftRequest(null, null,
            List.of(new OrderItemDraftDto(null, "Cookie box", -1, new BigDecimal("1.2501"), null)), "KWD", null)) >= 2);
    }
    @Test void confirmationRequiresVersion() {
        assertTrue(violations(new ConfirmOrderRequest(null)) > 0);
        assertTrue(violations(new ConfirmOrderRequest(-1L)) > 0);
        assertEquals(0, violations(new ConfirmOrderRequest(0L)));
    }
}
