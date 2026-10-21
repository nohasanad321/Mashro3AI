# REST contracts

These routes are implemented. Extraction currently uses a labelled-text demo parser in the local profile only.

| Method | Route | Request | Response |
|---|---|---|---|
| POST | /api/v1/order-drafts | CreateOrderDraftRequest | 201 OrderDraftResponse |
| POST | /api/v1/order-drafts/extract | ExtractOrderDraftRequest | 201 OrderDraftResponse |
| GET | /api/v1/order-drafts/{id} | — | OrderDraftResponse |
| PUT | /api/v1/order-drafts/{id} | SaveOrderDraftRequest | OrderDraftResponse |
| POST | /api/v1/order-drafts/{id}/clarification | — | ClarificationResponse |
| POST | /api/v1/order-drafts/{id}/confirm | ConfirmOrderRequest | 201 OrderResponse; 200 on replay |
| GET | /api/v1/orders/{id} | — | OrderResponse |
| GET | /api/v1/orders?page=0&size=20 | — | OrderPageResponse |

Manual creation and replacement differ: only replacement requires the existing version.
PUT replaces editable fields; omitted optional values become null. Source text is retained by the server.
Confirmation checks business-scoped draft ownership, expected version and completeness; replay of a confirmed
 draft returns its order before stale-version checks. Conflict responses use 409 ProblemDetail.
Unknown/out-of-business resources return 404. Invalid request shape returns 400; domain confirmation failures
return 422 with missing-field paths. A future model adapter must map timeout to 504 and provider unavailability to 503; no model adapter is connected yet.

Pagination is zero-based, size 1–100, with deterministic ordering by createdAt and ID.
Currency defaults belong to business settings; requests currently require the explicit uppercase code.
Monetary amounts are JSON numbers backed by BigDecimal; the UI must preserve decimal accuracy.
Collection DTOs are transport data; application services must not mutate their contents.

Example manual incomplete draft:

```json
{
  "customer": null,
  "delivery": {"method":"DELIVERY", "area":"Salmiya", "address":null,
    "requestedDate":null, "originalDateText":"Thursday", "notes":null},
  "items": [{"productId":null, "description":"Cookie box", "quantity":2,
    "unitPrice":null, "notes":null}],
  "currency":"KWD",
  "notes":null
}
```
