package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

public record OrderItemResponse(
    UUID productId, String description, int quantity,
    BigDecimal unitPrice, BigDecimal lineTotal, String notes
) {}
