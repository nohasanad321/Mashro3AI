package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

/** Full replacement of editable draft fields. businessId is resolved server-side. */
public record SaveOrderDraftRequest(
    @NotNull @PositiveOrZero Long version,
    @Valid CustomerDetailsDto customer,
    @Valid DeliveryDetailsDto delivery,
    @NotNull @Size(max = 100) List<@NotNull @Valid OrderItemDraftDto> items,
    @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
    @Size(max = 2000) String notes
) {}
