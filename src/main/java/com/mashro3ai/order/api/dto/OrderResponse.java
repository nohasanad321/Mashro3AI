package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

public record OrderResponse(
    UUID id, UUID businessId, UUID draftId, long version, OrderStatus status,
    CustomerDetailsDto customer, DeliveryDetailsDto delivery,
    List<OrderItemResponse> items, String currency, BigDecimal total,
    String notes, Instant createdAt, Instant updatedAt
) {}
