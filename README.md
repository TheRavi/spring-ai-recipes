# spring-ai-recipes

Working examples for building AI features in Java with [Spring AI](https://spring.io/projects/spring-ai). Each recipe is a standalone Spring Boot project, paired with a write-up on [ravibuilds.dev](https://ravibuilds.dev/blog).

The goal: practical Spring AI integration code you can actually run, with the real gotchas spelled out — not a paraphrase of the official docs.

## Why this repo exists

Most AI engineering content for Java developers in 2026 still assumes Python. Spring AI has quietly matured into a serious option for calling LLMs from Java applications — provider-portable, dependency-injected, and integrated cleanly with the rest of Spring.

This repo is a growing collection of small, focused examples that demonstrate one capability each. Use them as starting points, reference implementations, or proof that "AI in Java" is no longer a future tense.

## Recipes

| # | Recipe | Provider | What it covers | Blog post |
|---|---|---|---|---|
| 01 | [hello-gemini](./01-hello-gemini) | Google Gemini (free) | First Spring AI call to Gemini's API — no credit card needed | [Spring AI + Gemini: A First API Call in Java](https://ravibuilds.dev/blog/spring-ai-gemini-first-api-call) |
| 02 | structured-output | _Coming soon_ | Returning typed Java POJOs from an LLM | _Coming soon_ |
| 03 | tool-calling | _Coming soon_ | Letting the LLM invoke your Java methods | _Coming soon_ |
| 04 | provider-portability | _Coming soon_ | Same code, Gemini vs OpenAI vs Ollama | _Coming soon_ |
| 05 | rag-basics | _Coming soon_ | Retrieval-augmented generation in Spring AI | _Coming soon_ |

## How to run any recipe

Each recipe is a self-contained Spring Boot project with its own README. The general pattern:

```bash
# 1. Move into the recipe folder
cd 01-hello-gemini

# 2. Set the required API key (see each recipe's README for which one)
export GEMINI_API_KEY=your-key-here

# 3. Run
./mvnw spring-boot:run
```

Each recipe's `README.md` covers the required environment variables, endpoints, and what to expect.

> **Don't hardcode API keys in `application.yml`** — use environment variables or `application-local.yml` (gitignored). Each recipe ships with a `.env.example` template.

## Prerequisites

Common across all recipes:

- **Java 25** (or Java 21 LTS minimum — update `<java.version>` in `pom.xml` if needed)
- **Maven 3.9+** — each recipe ships with the Maven wrapper (`./mvnw`)
- An **API key** for the provider used in that recipe — check the recipe's README

## Stack

All recipes use:

- **Spring Boot 3.5.14**
- **Spring AI 1.1.6** (stable GA)
- **Java 25**

Each recipe pins its own dependency versions explicitly via the Spring AI BOM — no relying on transitive defaults.

> **Why not Spring Boot 4 + Spring AI 2.0?** Spring AI 1.1.x targets Spring Framework 6 / Spring Boot 3.x. Spring AI 2.0 (required for Boot 4) is still in milestone as of this writing. These recipes will be updated to the 2.0 stable once it ships.

## Provider coverage

| Provider | Free tier | Recipe |
|---|---|---|
| Google Gemini | ✅ 1,500 req/day, no credit card | [01-hello-gemini](./01-hello-gemini) |
| OpenAI | ❌ No free tier (pay-as-you-go) |  |
| Anthropic Claude | ❌ $5 minimum credit purchase |  |
| Ollama (local) | ✅ Unlimited, runs locally |  |

## A note on Gemini model names

Gemini model names move fast. If a recipe's model name returns a `404` or a `429` with `limit: 0`, the model has likely been deprecated or shut down. Check the [current Gemini model list](https://ai.google.dev/gemini-api/docs/models) and update the model name in `application.yml`.

As of May 2026, the current free-tier models are:
- `gemini-2.5-flash` ← recommended starting point
- `gemini-2.5-flash-lite`
- `gemini-2.5-pro`

`gemini-2.0-flash` and `gemini-1.5-flash` are deprecated and returning errors — don't use them.

## About

Built and maintained by [Ravi Kumar](https://www.linkedin.com/in/thekumar) — senior software engineer working in Java and AI engineering, writing about it at [ravibuilds.dev](https://ravibuilds.dev).

If you've built with Spring AI and hit gotchas these recipes miss, open an issue. Genuinely useful reports are welcome.

## License

MIT. Use these recipes however you like.