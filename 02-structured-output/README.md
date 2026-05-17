# 02 - Structured output

Take freeform text in, get a typed Java record out. No JSON parsing, no schema-by-hand, no regex.

This recipe demonstrates Spring AI's structured output API using a bug report triager: paste in a customer bug report, get back a `TriagedReport` record with severity, component, suggested labels (with confidence scores), and a one-sentence summary.

Paired with the blog post: [Structured output in Spring AI: from text to typed Java in one line](https://ravibuilds.dev/blog/spring-ai-structured-output-gemini)

## What this recipe shows

- The fluent `.entity(YourClass.class)` API — Spring AI 1.1's one-line structured output
- Schema generation from a Java record (including nested records, enums, and lists)
- Using `@JsonPropertyOrder` to control the schema field order LLMs see
- A `defaultSystem` prompt that constrains the LLM's behavior across all requests

## Requirements

- Java 25
- Maven (the included `mvnw` wrapper works)
- A free Gemini API key from [aistudio.google.com/apikey](https://aistudio.google.com/apikey)

## Run it

```bash
# Option 1 — env var
export GEMINI_API_KEY=your-key-here
./mvnw spring-boot:run

# Option 2 — local profile (put your key in application-local.yml)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Then in another terminal:

```bash
curl -X POST http://localhost:8080/triage \
  -H 'Content-Type: application/json' \
  -d '{
    "bugReport": "When I click the export button on the analytics page nothing happens. No error, no download. Tried Chrome and Firefox. This is blocking our team from sending the monthly report to the board."
  }'
```

You should get back something like:

```json
{
  "summary": "The export button on the analytics page silently fails to trigger a download.",
  "severity": "HIGH",
  "component": "analytics",
  "suggestedLabels": [
    { "label": "frontend", "confidence": 0.9 },
    { "label": "export", "confidence": 0.95 },
    { "label": "blocker", "confidence": 0.8 }
  ]
}
```

## The interesting part

The entire LLM call is three lines in [`TriageService.java`](./src/main/java/com/triager/service/TriageService.java):

```java
return chatClient.prompt()
    .user(bugReport)
    .call()
    .entity(TriagedReport.class);
```

Spring AI derives a JSON schema from `TriagedReport`, appends it to the prompt as format instructions, calls Gemini, then deserializes the response back into the record. The enum, the nested `SuggestedLabel` record, and the list of those records all just work.

## Project structure

```
02-structured-output/
├── pom.xml
└── src/main/
    ├── java/com/triager/
    │   ├── StructuredOutputApplication.java
    │   ├── controller/
    │   │   └── TriageController.java
    │   ├── model/
    │   │   ├── Severity.java          # enum: CRITICAL, HIGH, MEDIUM, LOW
    │   │   ├── SuggestedLabel.java    # nested record with confidence
    │   │   ├── TriageRequest.java
    │   │   └── TriagedReport.java     # the LLM's output target
    │   └── service/
    │       └── TriageService.java     # the .entity() call
    └── resources/
        ├── application.yml            # reads ${GEMINI_API_KEY}
```

## What can go wrong

- **JSON parse errors from the LLM.** Gemini Flash occasionally emits invalid JSON for complex schemas — extra commentary, markdown fences, trailing commas. Lower `temperature` (already set to 0.2 here) and keep schemas shallow.
- **Enum values mismatched.** If the LLM responds with `"Critical"` instead of `"CRITICAL"`, deserialization fails. The schema generation handles this most of the time, but watch for it on edge cases.
- **Missing fields.** If the LLM omits a non-optional record field, you get a deserialization error. Either make fields nullable (use boxed types or `@JsonInclude`) or rely on the model returning all fields (works well at `temperature: 0.2`).

The accompanying blog post covers each of these with the actual error messages and fixes.
