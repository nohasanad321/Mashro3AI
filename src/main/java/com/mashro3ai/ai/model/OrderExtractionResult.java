package com.mashro3ai.ai.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Untrusted candidate values; services validate and map them before persistence. */
public record OrderExtractionResult(
    CustomerCandidate customer, DeliveryCandidate delivery,
    List<ItemCandidate> items, List<Warning> warnings
) {
    public record CustomerCandidate(String name, String phone, String email) {}
    public record DeliveryCandidate(String method, String area, String address,
                                    LocalDate requestedDate, String originalDateText, String notes) {}
    public record ItemCandidate(String description, Integer quantity, BigDecimal unitPrice, String notes, String sku) {
        public ItemCandidate(String description, Integer quantity, BigDecimal unitPrice, String notes) {
            this(description, quantity, unitPrice, notes, null);
        }
    }
    public record Warning(String code, String path, String message) {}
}
