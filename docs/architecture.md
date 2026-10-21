# Architecture decisions

- One Spring Boot deployment and PostgreSQL database, feature-oriented packages.
- REST controllers validate DTOs; application services coordinate domain rules and persistence.
- AI providers sit behind an application interface; provider output never becomes the public DTO contract.
- Model calls occur outside database transactions, with explicit timeouts. No AI SDK chosen yet.
- AI extracts drafts; seller review precedes confirmation. Missing values remain null.
- Drafts allow incomplete customer, delivery and item details. Confirmation completeness rules currently live in OrderDraftService.
- All business-owned records carry businessId. Repositories must scope every query to the current business.
- CurrentBusinessProvider currently has a local-only configured implementation. JWT phase adds Spring Security
  token validation and business-membership authorization. Client-provided business IDs are never trusted.
- UUID IDs, BigDecimal money, three decimal places for KWD. Currency codes need semantic validation in services.
- Persist timestamps as Instant; requested delivery date as LocalDate. Resolve relative dates against reference time
  and the business timezone; retain original text and flag ambiguity.
- Entities use @Version; draft updates and confirmation compare expected versions.
- Confirm transaction atomically creates one order per draft (unique constraint) and marks the draft confirmed.
  Repeat confirmation returns that existing order; concurrent confirmation must not create duplicates.
- Confirmed orders snapshot descriptions, customer/delivery details and prices.
- Unit prices and line totals are server-validated; calculated totals are never accepted from clients.
- Draft response validation findings are recomputed after edits. No guessed prices or confidence scores.
- Database schema, entities, services, API controllers and model adapters will be implemented incrementally.

## Delivery sequence

1. Foundation + DTO contracts (this commit).
2. Manual draft creation/read with persistence and business scoping.
3. Draft editing with version conflicts and validation findings.
4. Real AI extraction adapter and Arabic/English examples.
5. Clarification text and atomic repeat-safe confirmation.
6. Mobile-friendly demo screen, setup rehearsal and submission materials.
7. JWT authentication and business membership before real-user deployment.

The October 26 hackathon milestone is a planning assumption pending official requirements.

## Entity step

OrderDraft and OrderDraftItem are now mapped JPA entities in order.persistence. DraftCustomer and
DraftDelivery are embedded snapshots. The collection retains item order and cascades persistence.
Business IDs are supplied by the server-side service, never copied from a request.
Draft replacement and confirmation are implemented. Confirmed drafts reject further changes.

V2 creates draft, item and confirmed-order tables. ConfirmedOrder references an immutable confirmed draft,
whose owned customer, delivery and item values are the order snapshot; the confirmed draft cannot be edited.
Unique draft_id prevents duplicate confirmed orders, and optimistic locking protects competing edits.
H2 integration tests execute the migrations and validate mappings and the workflow. PostgreSQL and
concurrent request behavior should also be verified in the deployment environment.

## Current demonstration

The local demo implements manual drafts, deterministic labelled-text extraction behind OrderExtractionPort,
review/update, clarification text, confirmation, order retrieval and pagination, plus a browser screen.
A real AI provider, natural-language dates and Arabic parsing remain pending. Parser results pass DTO
validation before persistence, and the original source message is retained across edits.
