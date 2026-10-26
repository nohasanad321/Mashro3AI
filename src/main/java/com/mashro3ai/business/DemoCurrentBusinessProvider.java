package com.mashro3ai.business;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Local demonstration only; never accepts a business ID supplied by the client. */
@Component
@Profile({"local", "demo"})
public class DemoCurrentBusinessProvider implements CurrentBusinessProvider {
    private final UUID businessId;
    public DemoCurrentBusinessProvider(@Value("${mashro3.demo.business-id}") UUID businessId) {
        this.businessId = businessId;
    }
    @Override public UUID currentBusinessId() { return businessId; }
}
