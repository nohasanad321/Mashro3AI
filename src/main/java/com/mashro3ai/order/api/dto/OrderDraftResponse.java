package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

public record OrderDraftResponse(
    UUID id, UUID businessId, long version, DraftStatus status,
    String sourceMessage, CustomerDetailsDto customer, DeliveryDetailsDto delivery,
    List<OrderItemDraftDto> items, String currency, String notes,
    List<MissingFieldDto> missingFields, List<ExtractionWarningDto> warnings,
    UUID confirmedOrderId, Instant createdAt, Instant updatedAt
) {}
