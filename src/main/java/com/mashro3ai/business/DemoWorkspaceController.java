package com.mashro3ai.business;

import com.mashro3ai.ai.provider.LocalRetrievalOrderExtractor;
import com.mashro3ai.catalog.CatalogService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile({"demo", "local"})
@RequestMapping("/api/v1")
public class DemoWorkspaceController {
    private final CatalogService catalog;
    private final LocalRetrievalOrderExtractor retrieval;
    private final String currency;
    private final String timezone;

    public DemoWorkspaceController(CatalogService catalog, LocalRetrievalOrderExtractor retrieval,
            @Value("${mashro3.demo.currency}") String currency, @Value("${mashro3.demo.timezone}") String timezone) {
        this.catalog = catalog; this.retrieval = retrieval; this.currency = currency; this.timezone = timezone;
    }
    @GetMapping("/workspace") public Workspace workspace() {
        return new Workspace("The Little Batch", currency, timezone, "Single-user local prototype", catalog.products());
    }
    @GetMapping("/ai/status") public LocalRetrievalOrderExtractor.Status status() { return new LocalRetrievalOrderExtractor.Status(true, "Local catalogue retrieval", "Ready · no model or network required"); }
    public record Workspace(String name, String currency, String timezone, String mode, List<CatalogService.Product> products) {}
}
