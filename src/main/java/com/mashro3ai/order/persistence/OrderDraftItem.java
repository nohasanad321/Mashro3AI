package com.mashro3ai.order.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/** Nullable fields preserve incomplete extraction; confirmation validates completeness later. */
@Entity
@Table(name = "order_draft_items")
public class OrderDraftItem {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id", nullable = false)
    private OrderDraft draft;
    @Column(name = "product_id")
    private UUID productId;
    @Column(length = 300)
    private String description;
    private Integer quantity;
    @Column(name = "unit_price", precision = 15, scale = 3)
    private BigDecimal unitPrice;
    @Column(length = 1000)
    private String notes;

    protected OrderDraftItem() {}
    OrderDraftItem(OrderDraft draft, UUID productId, String description,
                   Integer quantity, BigDecimal unitPrice, String notes) {
        this.id = UUID.randomUUID(); this.draft = draft; this.productId = productId;
        this.description = description; this.quantity = quantity;
        this.unitPrice = unitPrice; this.notes = notes;
    }
    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getDescription() { return description; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getNotes() { return notes; }
}
