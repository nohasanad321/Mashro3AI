# Architecture decisions

- One Spring Boot deployment and PostgreSQL database, feature-oriented packages.
- REST controllers validate DTOs; application services coordinate domain rules and persistence.
- AI providers sit behind an application interface; provider output never becomes the public DTO contract.
- Model calls occur outside database transactions, with explicit timeouts. No AI SDK chosen yet.
- AI extracts drafts; seller review precedes confirmation. Missing values remain null.
- Drafts allow incomplete customer, delivery and item details. Confirmation rules are implemented in the domain later.
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
