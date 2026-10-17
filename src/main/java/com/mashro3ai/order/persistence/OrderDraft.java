package com.mashro3ai.order.persistence;

import jakarta.persistence.*;
import com.mashro3ai.order.domain.DraftStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "order_drafts", indexes = @Index(name = "idx_drafts_business", columnList = "business_id"))
public class OrderDraft {
    @Id
    private UUID id;
    @Column(name = "business_id", nullable = false, updatable = false)
    private UUID businessId;
    @Version
    private Long version;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DraftStatus status;
    @Column(name = "source_message", length = 10000)
    private String sourceMessage;
    @Embedded
    private DraftCustomer customer;
    @Embedded
    private DraftDelivery delivery;
    @OneToMany(mappedBy = "draft", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "item_position")
    private List<OrderDraftItem> items = new ArrayList<>();
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(length = 2000)
    private String notes;
    @Column(name = "confirmed_order_id", unique = true)
    private UUID confirmedOrderId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderDraft() {}

    public OrderDraft(UUID businessId, String currency, String sourceMessage,
                      DraftCustomer customer, DraftDelivery delivery, String notes) {
        this.id = UUID.randomUUID();
        this.businessId = Objects.requireNonNull(businessId, "businessId");
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("currency must be a three-letter uppercase code");
        }
        this.currency = currency; this.sourceMessage = sourceMessage;
        this.customer = customer; this.delivery = delivery; this.notes = notes;
        this.status = DraftStatus.DRAFT;
    }

    /** Creates an owned item; callers cannot attach an item belonging to a different draft. */
    public void addItem(UUID productId, String description, Integer quantity,
                        BigDecimal unitPrice, String notes) {
        if (status != DraftStatus.DRAFT) throw new IllegalStateException("Draft is already confirmed");
        items.add(new OrderDraftItem(this, productId, description, quantity, unitPrice, notes));
        // Touch the parent so child-only changes also participate in its optimistic lock.
        updatedAt = Instant.now();
    }

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate
    void beforeUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public Long getVersion() { return version; }
    public DraftStatus getStatus() { return status; }
    public String getSourceMessage() { return sourceMessage; }
    public DraftCustomer getCustomer() { return customer; }
    public DraftDelivery getDelivery() { return delivery; }
    public List<OrderDraftItem> getItems() { return Collections.unmodifiableList(items); }
    public String getCurrency() { return currency; }
    public String getNotes() { return notes; }
    public UUID getConfirmedOrderId() { return confirmedOrderId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
