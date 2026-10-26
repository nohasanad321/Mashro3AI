package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

/** Reference time is optional; the service will resolve it using the business timezone. */
public record ExtractOrderDraftRequest(
    @NotBlank @Size(max = 10000) String message,
    Instant referenceTimestamp,
    @Pattern(regexp = "demo|ollama") String engine
) {
    public ExtractOrderDraftRequest(String message, Instant referenceTimestamp) {
        this(message, referenceTimestamp, "demo");
    }
}
