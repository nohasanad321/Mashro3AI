# Architecture decisions

## Current implementation

Mashro3 AI is a feature-oriented Spring Boot modular monolith with one deployment. The default `demo` profile uses persistent H2; `local` uses PostgreSQL. Static browser assets are served by the same application.

Controllers validate transport DTOs, application services coordinate work, and JPA entities and business-scoped repositories handle persistence. The `customer`, `identity`, and `reporting` packages currently contain placeholders.

## Extraction and retrieval

`OrderExtractionPort` defines the extraction boundary and returns candidate data rather than public API DTOs. `DemoExtractionService` selects either the labelled-text parser or the offline catalogue matcher, resolves catalogue products/prices, validates candidates, and creates a draft.

There are no model calls, embedding models, vector indexes, or external AI services. Catalogue alias matching and rules support selected Arabic/English input. Knowledge lookup separately returns keyword-matched passages from `knowledge.json` with source IDs, or no sources. Neither path generates text with a language model.

Keep unknown values unset, preserve source messages and extraction warnings, and require owner review before confirmation. Unknown products, repeated mentions, negation, and unsupported phrasing are known retrieval limitations.

## Persistence and business rules

- UUID identifiers; BigDecimal monetary values with up to three fractional digits on item inputs.
- Persist timestamps as Instant and requested delivery dates as LocalDate. Resolve relative dates in the business timezone and retain original date text.
- Business IDs come from `CurrentBusinessProvider`, not client requests. The current implementation returns one server-configured business; this is not authentication or multi-user authorization.
- Repository lookups for business-owned records include the business ID.
- Drafts allow incomplete data. Completeness findings are recomputed by `OrderDraftService` after review edits.
- `OrderDraft` uses JPA `@Version`. Review updates and initial confirmation compare expected versions.
- Confirmation creates one `ConfirmedOrder` per draft and marks the draft confirmed in one transaction. A unique `draft_id` constraint prevents duplicate order records. Repeat confirmation returns the existing order.
- Confirmed drafts cannot be edited, so their customer, delivery, item, and price values serve as the order snapshot. The confirmed-order record references that draft.
- Totals are calculated on the server from the confirmed item values. They exclude tax and delivery fees.
- Catalogue prices ground natural-message extraction, but review accepts owner-entered prices. There is no price-edit permission layer.

## Database profiles and migrations

`demo` stores H2 data at `./data/mashro3ai.mv.db`, relative to the process working directory. `local` defaults to PostgreSQL `mashro3ai_demo`; Compose initially creates `mashro3ai`, so create the separate demo database or explicitly configure the intended database URL.

Flyway migrations V1–V3 define the schema and extraction provenance. Hibernate validates the schema. Preserve applied migration files and add new versions for schema changes.

Tests execute migrations and application flows against in-memory H2. PostgreSQL behavior and competing concurrent requests need additional environment-specific verification.

## Next implementation: order fulfillment

`OrderStatus` already lists `CONFIRMED`, `PREPARING`, `READY`, `COMPLETED`, and `CANCELLED`. These are currently vocabulary only: `ConfirmedOrder` has no persisted status/version, and the response mapper hard-codes status `CONFIRMED` and version `0`.

The next increment should introduce a new migration for status, version, and update timestamp; define allowed transitions; expose a business-scoped update operation with expected-version checks; and show status controls in the UI. Keep the original confirmed customer/item/price snapshot unchanged while fulfillment status evolves.

Follow with date/status filtering, editable business data, and stronger extraction rules. Before real-user deployment, add authentication and business-membership authorization, verify PostgreSQL and concurrency behavior, and establish operational controls. The prototype currently binds to loopback and is intended for synthetic local demonstration data.
