package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

public record DeliveryDetailsDto(
    DeliveryMethod method,
    @Size(max = 200) String area,
    @Size(max = 1000) String address,
    LocalDate requestedDate,
    @Size(max = 500) String originalDateText,
    @Size(max = 1000) String notes
) {}
