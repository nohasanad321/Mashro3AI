package com.mashro3ai.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import com.mashro3ai.order.domain.*;

/** Draft text only; this API does not send customer messages. */
public record ClarificationResponse(UUID draftId, String text, List<String> addressedFields) {}
