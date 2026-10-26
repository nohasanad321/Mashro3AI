# Mashro3 AI — hackathon submission brief

## Short description
A local-first Arabic/English assistant that turns customer messages into reviewed, persistent orders for home businesses in the GCC.

## Problem and value
Home business owners receive informal orders across chats. Quantities, addresses and delivery dates get missed, and copying messages into spreadsheets takes time. Mashro3 AI gives owners a structured order desk without removing their control over customer commitments.

## Solution
Paste a message, extract candidate details with a local language model, resolve missing information in a form, draft a clarification reply, and approve a saved order. A versioned business catalogue supplies prices; the model never decides them. Unknown products require manual pricing. Confirmation checks completeness and computes totals on the server.

## Technology
Java 25, Spring Boot 4.0.8, JPA, Flyway, H2/PostgreSQL, Ollama structured output, plain HTML/CSS/JavaScript. Default local model: qwen3:4b. The labelled parser is explicitly a workflow demonstration, not an AI implementation.

## 90-second narration
“I built Mashro3 AI for people running small businesses from their homes. Their orders arrive as conversation, but fulfilment needs clear details. Here is a customer asking for cookies and brownies. The assistant creates a draft, and we can see that the address is missing. Nothing gets confirmed without the owner. I add the address, save, and reload to demonstrate persistence. Prices come from the business catalogue, and the server calculates the total. When I confirm, we have one saved order, even if the request is retried. With Ollama running, this same flow accepts Arabic and English chat locally. My next step is testing this workflow with real owners before adding messaging integrations.”

## Submission checklist
- Public repository: https://github.com/nohasanad321/Mashro3AI
- Record the real AI flow after installing the model; disclose if demonstrating the parser instead.
- Record the review → save → confirm → reload flow.
- Supply a cover image, slides and video if required by the current submission form.
- A public hosted app is not included; this prototype is a local runnable application.
- Verify organizer schedule, selected partner track, pre-existing code rules, AI assistance disclosure and licensing before final submission.
- Organizer page checked 26 October 2026: https://lablab.ai/ai-hackathons/ai-genesis-2026 (currently says dates TBA).

No customer validation, accuracy rate, revenue or measured time saving is claimed. Sponsor-track compliance is not established.
