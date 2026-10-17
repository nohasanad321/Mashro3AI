package com.mashro3ai.order.persistence;

import jakarta.persistence.*;

/** Customer snapshot owned by the draft; fields may be unknown. */
@Embeddable
public class DraftCustomer {
    @Column(name = "customer_name", length = 200)
    private String name;
    @Column(name = "customer_phone", length = 40)
    private String phone;
    @Column(name = "customer_email", length = 254)
    private String email;

    protected DraftCustomer() {}
    public DraftCustomer(String name, String phone, String email) {
        this.name = name; this.phone = phone; this.email = email;
    }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
