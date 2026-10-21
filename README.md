# Mashro3 AI

Java 25 / Spring Boot 4.0.8 small-business order desk.

## Run the local demo

With JDK 25 and Docker Desktop installed, run from the project root:

```powershell
docker compose up -d
```

Before the first run, create the demo database once (skip if it already exists):

```powershell
docker compose exec postgres createdb -U mashro3ai mashro3ai_demo
```

Then build and start:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The local profile defaults to `mashro3ai_demo`, so it also works from a new terminal.
`DB_URL` can override that default. Existing data in the original `mashro3ai` database
is untouched; do not repair or rewrite its migration history to run this demo.

Open **http://localhost:8080/**. The local profile binds to loopback. Flyway creates
and validates the draft and order tables automatically, including the new V2 migration.
For Linux/macOS, use `./mvnw` instead of `.\mvnw.cmd`.

### Five-minute example

1. Click **Extract draft** using the prefilled synthetic cookie order.
2. Notice `delivery.address` is missing. Confirmation returns 422 until it is supplied.
3. In the review JSON, replace `"address": null` with `"address": "Demo block 1, street 2"`.
4. Click **Save changes**, then **Reload saved draft** to verify persistence.
5. Click **Confirm order**. The server calculates **12.250 KWD** (2 × 4.500 + 3.250).
6. Click **Reload confirmed order**, or refresh the page. The saved draft ID is kept locally.

**The local extraction adapter is a deterministic labelled-text parser, not an AI model.**
It exercises the provider boundary and full persistence workflow without external credentials.
It accepts `name`, `phone`, `email`, `method` (DELIVERY/PICKUP), `area`, `address`,
`date` (YYYY-MM-DD), and repeated `item: description | quantity | unit price` lines.
It does not interpret arbitrary chat, relative dates, Arabic labels, or infer prices.
Blank item quantity/price remain unknown and require review. Unknown labels are ignored;
review the source and extracted fields. Business currency defaults to KWD.

## Implemented API

- POST `/api/v1/order-drafts` — manual creation, incomplete details allowed.
- POST `/api/v1/order-drafts/extract` — local profile only, demo parser.
- GET/PUT `/api/v1/order-drafts/{id}` — read and version-checked replacement.
- POST `/api/v1/order-drafts/{id}/clarification` — draft text only; sends nothing.
- POST `/api/v1/order-drafts/{id}/confirm` — completeness check and confirmation.
- GET `/api/v1/orders/{id}` and GET `/api/v1/orders?page=0&size=20`.

PUT requires the current version. Stale writes return 409. Confirm returns 201 initially,
then 200 with the same order on replay, including when replaying an old expectedVersion.
Confirmed drafts cannot be edited; their owned fields form the immutable order snapshot.
Concurrent conflicting writes can return 409; reload and retry. Resources are business-scoped;
missing or other-business IDs return 404. Confirmation requires name, phone, fulfillment
method/date, at least one described item with quantity/price, and address/area for delivery.
Totals are item totals only; delivery fees, tax and discounts are not modeled yet.

## Validation

```powershell
.\mvnw.cmd clean verify
```

Tests use an isolated H2 database in PostgreSQL mode, execute Flyway migrations and Hibernate
schema validation, and exercise extraction, persisted edits, stale versions, confirmation,
replay and business isolation. PostgreSQL startup should also be checked with Docker locally.

Authentication and a real model adapter are pending. Use synthetic data in the local demo.
Outside the local profile, configure DB_URL, DB_USERNAME, DB_PASSWORD and an authenticated
CurrentBusinessProvider before running. Do not expose the demonstration publicly.
