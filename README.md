# Mashro3 AI · مشروع

A local-first Arabic/English order desk for home businesses in Kuwait and the GCC.
Turn a customer message into a draft, review missing details, approve the order and reload it later.

## Run on Windows (JDK 25)

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Open http://localhost:8080. No Docker, database installation, API key or Node build needed.
The **demo** profile stores data in `data/mashro3ai.mv.db`. Stop the app before backing up that file.
The existing **local** profile still uses PostgreSQL `mashro3ai_demo`; run `docker compose up -d` and create that database if needed.
Do not change existing Flyway checksums; V3 adds extraction provenance without editing V1/V2.
On macOS/Linux replace `.\mvnw.cmd` with `./mvnw`.

## Enable real local AI

Install Ollama from https://ollama.com/download/windows, open a new terminal, then:

```powershell
ollama pull qwen3:4b
```

Leave Ollama running. Choose **English chat** or **رسالة عربية**, then **Extract draft**.
The app uses `/api/chat` with a JSON schema, temperature 0 and thinking disabled.
On a CPU the first request may take several minutes. Timeout defaults to 180 seconds.
Configure `OLLAMA_MODEL`, `OLLAMA_BASE_URL` or `OLLAMA_TIMEOUT_SECONDS` before starting Spring Boot.
Use a local model to keep customer messages on your computer. Model installation requires internet.

**Labelled demo parser** is a separate deterministic mode, explicitly not AI. It works without Ollama.
It accepts name, phone, email, method, area, address, date (YYYY-MM-DD), and repeated
`Item: description | quantity | price` lines. It does not understand free-form chat or Arabic.
No silent fallback occurs if real AI fails. Invalid model data is rejected before persistence.

## Three-minute demo

1. Click **Labelled example**, then **Extract draft**. These are synthetic customer details.
2. Notice the missing full address. Enter `Demo block 1, street 2, building 3`.
3. Save the review. Reload it. The address survives because it is stored in the database.
4. Draft a missing-details reply in English or Arabic. It is never sent automatically.
5. Confirm. The server calculates **12.250 KWD** for two cookie boxes and one brownie box.
6. Refresh the browser and open the order from Saved work.
7. With Ollama installed, repeat using natural-language English or Arabic and explain catalogue pricing.

## What is implemented

- Java 25 / Spring Boot 4.0.8 modular monolith, REST `/api/v1`.
- Real Ollama adapter, structured output schema and explicit model readiness status.
- Versioned bilingual catalogue in `src/main/resources/catalog.json`; model output cannot set prices.
- Editable customer, delivery, items and owner notes; missing-field checks and manual entry.
- Durable extraction provenance, drafts and immutable confirmed orders; optimistic locking and replay-safe confirmation.
- English/Arabic clarification templates, excluding owner-only price questions.
- Saved draft/order pages with pagination; responsive UI with no external asset dependencies.
- H2 file demo and PostgreSQL local profiles; Flyway migrations and CI tests.

## Prototype boundary

Single business, no authentication; bound to loopback. Do not expose this profile publicly.
No WhatsApp integration, automatic sending, voice transcription, payments, stock tracking, tax or delivery fee calculation.
Arabic model accuracy depends on model quality and requires owner review. This is not a production service.
The web UI is English with Arabic input and Arabic reply support; it is not fully localized.

See [submission brief](docs/submission.md), [code review guide](docs/code-review.md), and [API contracts](docs/api-contracts.md).
