# Mashro3 AI

Java 25 / Spring Boot 4.0.8 modular monolith for an AI-assisted small-business back office.

## Current scope

Project foundation, validated REST DTO contracts and initial draft JPA entities.
Order controllers, business logic, table migrations, AI adapters and authentication are pending.
**Entity step:** the application needs the upcoming V2 migration before it can start against PostgreSQL.
No business API endpoints exist yet. `/actuator/health` is available when the application starts.

## Run locally

Install JDK 25 and Docker Desktop. Maven is downloaded by the committed Maven Wrapper.

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows PowerShell use `./mvnw.cmd` instead of `./mvnw`.
Check `http://localhost:8080/actuator/health`.

```bash
./mvnw clean verify
```

Tests validate DTO constraints without requiring PostgreSQL. Running the application requires PostgreSQL.
The `local` profile binds to loopback and uses synthetic demo-business settings. Authentication is deferred;
this is not ready for public deployment. Outside that profile, provide DB_URL, DB_USERNAME and DB_PASSWORD.
Do not commit secrets or real customer data.

## Structure

Feature packages: business, order, ai; reserved packages: catalog, customer, identity, reporting.
Inside order: api/dto, application, domain, persistence. DTOs are Java records, separate from entities.
See [architecture](docs/architecture.md) and [API contracts](docs/api-contracts.md).

## Next coding session

1. Review draft DTO fields and confirmation requirements.
2. Implement OrderDraft entity and child item mapping; add Flyway V2.
3. Add business-scoped repository and transactional manual draft creation service.
4. Expose POST /api/v1/order-drafts and verify save/read against PostgreSQL.

Then add extraction, review/update and repeat-safe confirmation, one use case at a time.
