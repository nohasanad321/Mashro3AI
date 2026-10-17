package com.mashro3ai.order.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.mashro3ai.order.domain.DeliveryMethod;

@Embeddable
public class DraftDelivery {
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", length = 20)
    private DeliveryMethod method;
    @Column(name = "delivery_area", length = 200)
    private String area;
    @Column(name = "delivery_address", length = 1000)
    private String address;
    @Column(name = "delivery_requested_date")
    private LocalDate requestedDate;
    @Column(name = "delivery_original_date_text", length = 500)
    private String originalDateText;
    @Column(name = "delivery_notes", length = 1000)
    private String notes;

    protected DraftDelivery() {}
    public DraftDelivery(DeliveryMethod method, String area, String address,
                         LocalDate requestedDate, String originalDateText, String notes) {
        this.method = method; this.area = area; this.address = address;
        this.requestedDate = requestedDate; this.originalDateText = originalDateText; this.notes = notes;
    }
    public DeliveryMethod getMethod() { return method; }
    public String getArea() { return area; }
    public String getAddress() { return address; }
    public LocalDate getRequestedDate() { return requestedDate; }
    public String getOriginalDateText() { return originalDateText; }
    public String getNotes() { return notes; }
}
