package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

/** Null quantity or price means unknown, never zero by default. */
public record OrderItemDraftDto(
    UUID productId,
    @Size(max = 300) String description,
    @Positive Integer quantity,
    @DecimalMin("0.0") @Digits(integer = 12, fraction = 3) BigDecimal unitPrice,
    @Size(max = 1000) String notes
) {}
