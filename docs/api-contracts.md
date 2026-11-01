# REST contracts

All routes below are implemented and use the `/api/v1` prefix. The `demo` and `local` profiles provide extraction, workspace metadata, and the configured single-business identity. Authentication is not implemented.

| Method | Route | Request / response |
| --- | --- | --- |
| GET | /workspace | Business settings and catalogue products |
| GET | /ai/status | Local retrieval readiness; no model health check |
| POST | /order-drafts | CreateOrderDraftRequest → 201 OrderDraftResponse |
| POST | /order-drafts/extract | ExtractOrderDraftRequest → 201 OrderDraftResponse |
| GET | /order-drafts?page=0&size=20 | Paginated drafts, including confirmed draft records |
| GET | /order-drafts/{id} | OrderDraftResponse |
| PUT | /order-drafts/{id} | SaveOrderDraftRequest → OrderDraftResponse |
| POST | /order-drafts/{id}/clarification?language=en | ClarificationResponse; use `ar` for Arabic |
| POST | /order-drafts/{id}/confirm | ConfirmOrderRequest → 201 OrderResponse; 200 on replay |
| GET | /orders/{id} | OrderResponse |
| GET | /orders?page=0&size=20 | OrderPageResponse |
| GET | /knowledge?question=delivery%20address | Message and up to three source passages |

## Extraction

Send `message`, explicit `engine`, and optional ISO-8601 `referenceTimestamp`.

- `engine: "demo"`: labelled-text parser. Each item is `Item: description | quantity | price`; dates use `YYYY-MM-DD`.
- `engine: "local"`: bilingual catalogue alias retrieval and explicit extraction rules. Prices are resolved from the catalogue. No language model or generation is involved.
- When omitted or null in JSON, the current service selects local retrieval. The two-argument Java convenience constructor selects `demo`. Clients should set the engine explicitly.
- Relative dates use the supplied reference timestamp, or the server's current instant, in the configured business timezone.
- Invalid parsed values return 400 before draft persistence. Original source text and warnings are retained with the draft.

## Editing and confirmation

Manual creation allows incomplete drafts. Replacement requires the current `version`; PUT replaces editable fields and omitted optional values become null. Source text and extraction warnings are retained by the server. Review may override prices; there is no price-edit permission layer.

Confirmation requires `expectedVersion`, business-scoped ownership, and complete required fields. Delivery requires area and address; pickup does not. Both require a customer name, phone, requested date, and at least one item with description, quantity, and price.

Confirmation transactionally creates an order and marks its draft confirmed. Confirmed drafts reject edits. Replay returns the existing order before stale-version checks. Order status and version are currently returned as `CONFIRMED` and `0`; status updates are not implemented.

Clarification prepares text without sending it. It excludes missing unit prices from customer questions, leaving those for owner review.

## Errors and data conventions

- 400: invalid request or extraction values; DTO validation responses include `errors`.
- 404: unknown or out-of-business draft/order.
- 409: stale draft version, edits to confirmed drafts, or persistence conflicts.
- 422: incomplete confirmation, with `missingFields` paths.

Responses use ProblemDetail for these errors. Pagination is zero-based with size 1–100 and deterministic descending order by creation time and ID. Currency is an explicit uppercase code in manual draft requests; extraction uses the business currency. Monetary values are JSON numbers backed by BigDecimal; KWD inputs support three decimal places. The server calculates totals, excluding taxes and delivery fees.

The separate health endpoint is `GET /actuator/health`.
