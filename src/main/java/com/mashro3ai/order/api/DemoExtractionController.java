package com.mashro3ai.order.api;
import com.mashro3ai.order.api.dto.*;
import com.mashro3ai.order.application.DemoExtractionService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@Profile({"local", "demo"})
public class DemoExtractionController {
    private final DemoExtractionService service;
    public DemoExtractionController(DemoExtractionService service) { this.service=service; }
    @PostMapping("/api/v1/order-drafts/extract")
    public ResponseEntity<OrderDraftResponse> extract(@Valid @RequestBody ExtractOrderDraftRequest r) {
        var result=service.extract(r); return ResponseEntity.created(URI.create("/api/v1/order-drafts/"+result.id())).body(result);
    }
}
