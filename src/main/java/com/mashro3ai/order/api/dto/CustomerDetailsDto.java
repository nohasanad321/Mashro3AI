package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

/** All fields are optional while an order is a draft. */
public record CustomerDetailsDto(
    @Size(max = 200) String name,
    @Size(max = 40) String phone,
    @Email @Size(max = 254) String email
) {}
