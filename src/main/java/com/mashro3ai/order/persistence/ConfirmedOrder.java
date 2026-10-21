package com.mashro3ai.order.persistence;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
/** Confirmed drafts are immutable, so their owned data is the order snapshot. */
@Entity
@Table(name = "confirmed_orders")
public class ConfirmedOrder {
    @Id private UUID id;
    @Column(name="business_id", nullable=false) private UUID businessId;
    @Column(name="draft_id", nullable=false, unique=true) private UUID draftId;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    protected ConfirmedOrder() {}
    public ConfirmedOrder(OrderDraft draft) {
        id=UUID.randomUUID(); businessId=draft.getBusinessId(); draftId=draft.getId(); createdAt=Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public UUID getDraftId() { return draftId; }
    public Instant getCreatedAt() { return createdAt; }
}
