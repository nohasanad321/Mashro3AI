package com.mashro3ai.ai.application;

import com.mashro3ai.ai.model.ExtractionContext;
import com.mashro3ai.ai.model.OrderExtractionResult;

/** Provider boundary. Implementations must enforce a bounded model-call timeout. */
public interface OrderExtractionPort {
    OrderExtractionResult extract(String message, ExtractionContext context);
}
