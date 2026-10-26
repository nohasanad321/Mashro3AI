package com.mashro3ai.order.api;
import com.mashro3ai.order.api.dto.*;
import com.mashro3ai.order.application.OrderDraftService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OrderDraftController {
    private final OrderDraftService service;
    public OrderDraftController(OrderDraftService service) { this.service=service; }
    @PostMapping("/order-drafts")
    public ResponseEntity<OrderDraftResponse> create(@Valid @RequestBody CreateOrderDraftRequest r) {
        var result=service.create(r); return ResponseEntity.created(URI.create("/api/v1/order-drafts/"+result.id())).body(result);
    }
    @GetMapping("/order-drafts/{id}") public OrderDraftResponse get(@PathVariable UUID id) { return service.get(id); }
    @PutMapping("/order-drafts/{id}") public OrderDraftResponse update(@PathVariable UUID id,@Valid @RequestBody SaveOrderDraftRequest r) { return service.update(id,r); }
    @GetMapping("/order-drafts") public OrderDraftService.DraftPage drafts(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return service.listDrafts(page,size); }
    @PostMapping("/order-drafts/{id}/clarification") public ClarificationResponse clarification(@PathVariable UUID id,@RequestParam(defaultValue="en") String language) { return service.clarification(id,language); }
    @PostMapping("/order-drafts/{id}/confirm")
    public ResponseEntity<OrderResponse> confirm(@PathVariable UUID id,@Valid @RequestBody ConfirmOrderRequest r) {
        var result=service.confirm(id,r);
        return ResponseEntity.status(result.created()?HttpStatus.CREATED:HttpStatus.OK).location(URI.create("/api/v1/orders/"+result.order().id())).body(result.order());
    }
    @GetMapping("/orders/{id}") public OrderResponse order(@PathVariable UUID id) { return service.order(id); }
    @GetMapping("/orders") public OrderPageResponse orders(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return service.list(page,size); }
}
