# 01 — Hello Gemini

The simplest possible Spring AI integration with Google's Gemini API.

A Spring Boot REST endpoint that accepts a message, sends it to Gemini, and returns the response as a typed JSON object.

Free to run — Google's Gemini Developer API has a generous free tier (1,500 requests/day on Gemini 2.0 Flash) and **does not require a credit card**.

**Companion blog post:** [Spring AI + Gemini: A First API Call in Java](https://ravibuilds.dev/blog/spring-ai-gemini-first-api-call)

## What this demonstrates

- Wiring `spring-ai-starter-model-google-genai` into a Spring Boot 3.5 project
- Configuring the Gemini Developer API (free tier) via `application.yml`
- Calling Gemini with the auto-configured `ChatClient.Builder`
- Returning typed responses via Java records

## Stack

- **Java 25**
- **Spring Boot 3.5.14**
- **Spring AI 1.1.6** (stable GA)
- **Gemini 2.0 Flash** (free tier)

## Prerequisites

1. **Java 25** — verify with `java -version`. If your build fails with an "unsupported class file version" error, fall back to Java 21 or 17 (still LTS) by changing `<java.version>` in `pom.xml`.
2. **Maven 3.9+** — verify with `mvn -v`.
3. A **free Gemini API key** from [aistudio.google.com/apikey](https://aistudio.google.com/apikey). Just sign in with a Google account — no credit card.

## Run it

### 1. Get a free Gemini API key

Visit [aistudio.google.com/apikey](https://aistudio.google.com/apikey), sign in with a Google account, click "Create API key." Copy the key. That's it — no billing setup, no credit card.

### 2. Set the environment variable

```bash
export GEMINI_API_KEY=your-real-key-here
```

Or copy `.env.example` to `.env`, fill in your key, and source it:

```bash
cp .env.example .env
# edit .env with your real key
set -a; source .env; set +a
```

### 3. Start the app

```bash
./mvnw spring-boot:run
```

You'll see Spring Boot start up on port 8080.

### 4. Call the endpoint

```bash
curl "http://localhost:8080/chat?message=Explain%20Spring%20AI%20in%20one%20sentence."
```

You should see a JSON response with your prompt and Gemini's reply:

```json
{
  "prompt": "Explain Spring AI in one sentence.",
  "reply": "Spring AI is an application framework..."
}
```

To use the default prompt (Gemini introduces itself briefly):

```bash
curl "http://localhost:8080/chat"
```

## Run the tests

```bash
./mvnw test
```

The smoke test verifies the Spring context loads cleanly with the Google GenAI auto-configuration — it uses a dummy API key, so it does not make real API calls.

## Project structure

```
01-hello-gemini/
├── README.md                       ← This file
├── pom.xml                         ← Maven config with Spring AI BOM
├── .env.example                    ← Template for local environment variables
├── .gitignore                      ← Ignores target/, IDE files, .env
└── src/
    ├── main/
    │   ├── java/com/hellogemini/
    │   │   ├── HelloGeminiApplication.java   ← Spring Boot entry point
    │   │   └── ChatController.java           ← The /chat endpoint + ChatResponse record
    │   └── resources/
    │       └── application.yml               ← Gemini + model config
    └── test/
        └── java/com/hellogemini/
            └── HelloGeminiApplicationTests.java   ← Context-load smoke test
```

## Key dependency

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>
```

Version is managed by the Spring AI BOM (1.1.6) in `pom.xml`.

## Configuration notes

- **Model:** `gemini-2.0-flash` — fast, capable, generous free tier limits
- **Max output tokens:** `1024` — enough for short responses
- **Temperature:** `0.7` — balanced middle ground. Use `0.0–0.3` for factual tasks, `0.8–1.0` for creative ones

## Two auth modes — important

Spring AI's Google GenAI starter supports two authentication modes:

1. **Gemini Developer API (free tier, what this recipe uses)** — set only `spring.ai.google.genai.api-key`. No GCP project needed.
2. **Vertex AI (paid, production)** — set `project-id` and `location` instead. Uses GCP credentials.

If you accidentally set both `api-key` and `project-id`, you may get unexpected auth errors. For the free tier, only set the API key.

## Troubleshooting

**Java version errors at build time.**
Spring Boot 3.5.14 supports Java 17 through 25. If you see `Unsupported class file major version`, verify with `java -version` that you're on Java 25. To use an older JDK, change `<java.version>25</java.version>` in `pom.xml` to `21` or `17`.

**`GEMINI_API_KEY` not found error.**
Make sure the variable is set in the same shell session you run `mvn spring-boot:run` from. In IDEs, set it in your run configuration explicitly — environment variables don't always inherit from your shell.

**401 Authentication Error.**
The API key is invalid or expired. Regenerate at [aistudio.google.com/apikey](https://aistudio.google.com/apikey).

**`BeanCreationException` at startup.**
Usually means the BOM version doesn't match the starter version. The BOM in `pom.xml` controls everything — don't add explicit versions to Spring AI dependencies.

**Rate limit errors (429).**
The free tier allows 1,500 requests/day and 15 requests/minute. If you exceed this, wait a minute or upgrade to a paid tier.

**Model not found.**
The Gemini model names move fast. If `gemini-2.0-flash` is unavailable, try `gemini-1.5-flash` or check the [current Gemini models](https://ai.google.dev/gemini-api/docs/models).

## Free tier limits (at a glance)

- 1,500 requests per day
- 15 requests per minute
- 1 million token context window
- No credit card required
- Quota resets daily

These limits are for prototyping and development. For production, upgrade to a paid tier or use Vertex AI.

## Next steps

Once this is running, the natural next recipes in this series will cover:

- **Structured output** — return typed Java POJOs from Gemini instead of raw strings
- **Tool calling** — let Gemini invoke your Java methods
- **Provider portability** — same code, different LLM providers (Gemini, Claude, OpenAI, Ollama)

See the [full walkthrough on the blog](https://ravibuilds.dev/blog/spring-ai-gemini-first-api-call) for the why behind each choice.
