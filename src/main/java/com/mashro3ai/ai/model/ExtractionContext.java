package com.mashro3ai.ai.model;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.UUID;

public record ExtractionContext(UUID businessId, Instant referenceTimestamp,
                                ZoneId businessTimezone, Currency currency) {}
