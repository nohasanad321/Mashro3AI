package com.mashro3ai.order.persistence;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import com.mashro3ai.order.domain.DraftStatus;
import static org.junit.jupiter.api.Assertions.*;

class OrderDraftTest {
    @Test void incompleteDraftPreservesUnknownValues() {
        var businessId = UUID.randomUUID();
        var draft = new OrderDraft(businessId, "KWD", null, null, null, null);
        draft.addItem(null, "Cookie box", null, null, null);
        assertEquals(businessId, draft.getBusinessId());
        assertEquals(DraftStatus.DRAFT, draft.getStatus());
        assertNull(draft.getItems().getFirst().getQuantity());
        assertNull(draft.getItems().getFirst().getUnitPrice());
        assertNotNull(draft.getItems().getFirst().getId());
        assertThrows(UnsupportedOperationException.class, () -> draft.getItems().clear());
    }
    @Test void requiresBusinessAndCurrency() {
        assertThrows(NullPointerException.class, () -> new OrderDraft(null, "KWD", null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new OrderDraft(UUID.randomUUID(), "kwd", null, null, null, null));
    }
}
