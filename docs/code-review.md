# Review the code with Noha

Start with a single request, not the whole repository.

1. `order/api/DemoExtractionController.java`: receives a validated message and engine choice.
2. `order/application/DemoExtractionService.java`: chooses the adapter, resolves catalogue products and validates candidate values.
3. `ai/provider/OllamaOrderExtractor.java`: sends a schema-constrained request to Ollama. This is the external integration boundary.
4. `catalog/CatalogService.java`: provides owner-defined prices and product identities.
5. `order/application/OrderDraftService.java`: saves, checks missing fields, guards versions and confirms orders.
6. `order/persistence/OrderDraft.java`: owns the editable state and blocks edits after confirmation.
7. `static/app.js`: connects the review form to the REST endpoints.

Java paths are relative to `src/main/java/com/mashro3ai`; static paths are under `src/main/resources`.

## Good first contributions

- Add a product and its Arabic aliases to `catalog.json`; demonstrate extraction and explain the price source.
- Improve one Arabic clarification phrase in `OrderDraftService.clarification`.
- Add a business rule to `missing`, with a test showing what confirmation should reject.

Discuss why unknown prices remain null, why model output needs validation, why confirmation is idempotent, and why a stale version must fail before moving to additional features.
