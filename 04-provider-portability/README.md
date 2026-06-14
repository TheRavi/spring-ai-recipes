# 04 — Provider portability

Run the exact same triager code against Gemini and against any model on OpenRouter, switching with a flag.

This recipe takes the structured-output triager from recipe 02 and runs it on two different providers without changing a line of the Java. The service, the controller, and the `TriagedReport` record are untouched. Only the build profile and the config change.

Paired with the blog post: [Provider portability in Spring AI: Gemini, OpenRouter, and where the abstraction leaks](https://ravibuilds.dev/blog/spring-ai-provider-portability)

## The idea

Spring AI's `ChatClient` is provider-agnostic. Your application code talks to the abstraction, not to Gemini or OpenAI directly. In theory you can swap providers freely.

In practice there are two layers of portability here, and they behave differently:

1. **Spring AI's abstraction.** Your Java code stays identical across providers. But different *native* providers need different starters on the classpath, and you cannot put two native chat starters side by side without Spring failing to pick a `ChatModel`. So switching native providers means switching the starter, which this recipe does with Maven profiles.

2. **OpenRouter's abstraction.** OpenRouter exposes hundreds of models behind one OpenAI-compatible endpoint. Once you are pointed at OpenRouter, switching from Claude to Llama to Gemini is genuinely one config line, because they all ride the same protocol and the same starter.

The recipe shows both.

## Requirements

- Java 21+
- Maven (the included `mvnw` wrapper works)
- A free Gemini API key from [aistudio.google.com/apikey](https://aistudio.google.com/apikey)
- An OpenRouter API key from [openrouter.ai/keys](https://openrouter.ai/keys) (has free models)

## Setup

```bash
cp .env.example .env
# edit .env and add whichever key(s) you'll use
set -a && source .env && set +a
```

## Run against Gemini (default)

```bash
./mvnw spring-boot:run
```

This activates the `gemini` Maven profile (the default), which puts the native Google GenAI starter on the classpath, and the `gemini` Spring profile, which supplies the Gemini config.

## Run against OpenRouter

```bash
./mvnw spring-boot:run -Popenrouter -Dspring-boot.run.profiles=openrouter
```

Two switches happen here, and you need both:

- `-Popenrouter` is the **Maven** profile. It swaps the classpath dependency from the Google GenAI starter to the OpenAI starter.
- `-Dspring-boot.run.profiles=openrouter` is the **Spring** profile. It loads `application-openrouter.yml` instead of `application-gemini.yml`.

The Maven profile decides *which library is compiled in*. The Spring profile decides *which config is read*. They are different mechanisms and the recipe needs both to line up.

## Confirm the swap

```bash
curl http://localhost:8080/provider
```

Returns the active model label, so you can confirm which provider answered before you start comparing outputs.

## Test the triager

Identical request, whichever provider is active:

```bash
curl -X POST http://localhost:8080/triage \
  -H 'Content-Type: application/json' \
  -d '{"bugReport": "The CSV export on the reports page returns an empty file when the date range is over 90 days. Customers on the enterprise plan are blocked from their monthly reporting."}'
```

Run it once under Gemini, once under OpenRouter, and compare the `TriagedReport` you get back. Same code, same prompt, same schema. The differences you see are pure provider differences.

## Swapping models within OpenRouter

This is the part that is genuinely one line. In `application-openrouter.yml`, change:

```yaml
model: anthropic/claude-3.5-sonnet
```

to any model id from [openrouter.ai/models](https://openrouter.ai/models), for example `meta-llama/llama-3.3-70b-instruct` or `deepseek/deepseek-chat`. No dependency change, no code change, no rebuild beyond the restart. That is OpenRouter's portability layer doing the work.

## What can go wrong

- **Ambiguous ChatModel bean at startup.** If you put both native starters (`google-genai` and `openai`) on the classpath at once, Spring cannot decide which `ChatModel` to inject and the app fails to start. That is why this recipe uses Maven profiles to keep exactly one chat starter compiled in. If you want both available at runtime, you have to define qualified beans by hand.
- **Maven profile and Spring profile out of sync.** The most common mistake is switching one but not the other, for example `-Popenrouter` without `-Dspring-boot.run.profiles=openrouter`. You then compile the OpenAI starter but try to load Gemini config (or vice versa), and the startup error is confusing. Switch both together.
- **Structured output reliability varies by model.** The `.entity()` call relies on the model returning clean JSON for the schema. Strong models do this consistently. Smaller or cheaper OpenRouter models sometimes wrap JSON in prose or miss fields, which surfaces as a deserialization error. This is the abstraction leaking: same code, different reliability.
- **Tool calling support varies by model.** Not every model on OpenRouter supports tool calling, and some support it inconsistently. If you take recipe 03's tools to a random OpenRouter model, do not assume parity. Check the model's capabilities on the OpenRouter model page first.
- **OpenRouter adds a hop.** Routing through OpenRouter means an extra network hop and dependence on OpenRouter's uptime, rate limits, and pricing margin. Portability is not free. You trade provider-native directness for cross-model flexibility.

## Project structure

```
04-provider-portability/
├── pom.xml                       # two Maven profiles: gemini (default), openrouter
├── .env.example
└── src/main/
    ├── java/com/triager/
    │   ├── ProviderPortabilityApplication.java
    │   ├── controller/
    │   │   └── TriageController.java      # /triage and /provider
    │   ├── model/
    │   │   ├── Severity.java
    │   │   ├── SuggestedLabel.java
    │   │   ├── TriageRequest.java
    │   │   └── TriagedReport.java
    │   └── service/
    │       └── TriageService.java         # zero provider-specific code
    └── resources/
        ├── application.yml                # provider-agnostic
        ├── application-gemini.yml         # gemini profile config
        └── application-openrouter.yml     # openrouter profile config
```
