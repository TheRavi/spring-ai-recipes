# 03 — Tool calling

Give Gemini two tools and a real database, then let it decide which tool to call.

This recipe extends the bug-triager world from recipe 02. Instead of classifying a single report, the assistant can now answer questions by querying a real (in-memory H2) database through tools the model invokes on its own:

- `findSimilarBugs` — searches a history of past bug reports
- `getServiceStatus` — checks the current live status of a component

The interesting part is not that the tools work. It is watching the model choose between them: a question about whether something is broken *right now* triggers the status tool; a question about whether an issue was *seen before* triggers the history tool; a question that asks both triggers both.

Paired with the blog post: [Tool calling in Spring AI: letting Gemini query your database](https://ravibuilds.dev/blog/spring-ai-tool-calling-gemini)

## Requirements

- Java 21+
- Maven (the included `mvnw` wrapper works)
- A free Gemini API key from [aistudio.google.com/apikey](https://aistudio.google.com/apikey)

## Run it

```bash
cp .env.example .env
# edit .env and paste in your real key

set -a && source .env && set +a
./mvnw spring-boot:run
```

The H2 database is created in memory and seeded from `schema.sql` and `data.sql` on startup. No external database needed.

## Try it

The fun is in watching which tool the model picks. Run these and watch the console — each tool prints when it is called.

**Calls `findSimilarBugs` only** (a question about the past):

```bash
curl -X POST http://localhost:8080/ask \
  -H 'Content-Type: application/json' \
  -d '{"question": "Have we seen problems with the analytics export before? How were they fixed?"}'
```

**Calls `getServiceStatus` only** (a question about right now):

```bash
curl -X POST http://localhost:8080/ask \
  -H 'Content-Type: application/json' \
  -d '{"question": "Is the analytics service working right now?"}'
```

**Calls both tools** (a question that needs past and present):

```bash
curl -X POST http://localhost:8080/ask \
  -H 'Content-Type: application/json' \
  -d '{"question": "Is export broken right now, and have we hit this issue before?"}'
```

**Calls neither tool** (general question):

```bash
curl -X POST http://localhost:8080/ask \
  -H 'Content-Type: application/json' \
  -d '{"question": "What is a good severity level for a cosmetic UI bug?"}'
```

Watch the application console. You will see lines like `>> Tool called: getServiceStatus(component=analytics)` showing exactly what the model decided to invoke.

## The interesting part

The whole tool-calling setup is two annotations and one method call.

The tools, in [`BugTools.java`](./src/main/java/com/triager/tools/BugTools.java):

```java
@Tool(description = "Search the history of past bug reports ...")
public List<PastBug> findSimilarBugs(
    @ToolParam(description = "A component name or keyword") String term
) { ... }
```

Wiring them in, in [`AssistantService.java`](./src/main/java/com/triager/service/AssistantService.java):

```java
chatClient.prompt()
    .user(question)
    .tools(bugTools)
    .call()
    .content();
```

That is it. Spring AI inspects the `@Tool` methods, generates schemas from their signatures, sends those schemas to Gemini with the prompt, and when the model asks to call a tool, Spring AI runs the Java method and feeds the result back to the model automatically. The model can call multiple tools, reason over the combined results, and produce a final answer in a single `.call()`.

## The tool descriptions are prompt engineering

The single most important thing in this recipe is the text inside `@Tool(description = "...")`. That description is the only information the model has when deciding whether to call the tool. Vague descriptions cause the wrong tool to be called, or no tool at all.

The descriptions here are written to draw a sharp line between "right now" (status) and "seen before" (history). If you blur that line, the model starts calling the wrong tool on ambiguous questions. Try editing a description to be vaguer and watch the selection accuracy drop. It is the fastest way to feel how much the model leans on these strings.

## What can go wrong

- **`@Tool` methods not detected.** If the model never calls your tools, confirm the tool object is passed via `.tools(bugTools)` (per-request) or `.defaultTools(...)` (on the builder), and that the class is a Spring bean. Spring AI 1.1 had reported issues detecting `@Tool` methods in certain registration patterns ([#5134](https://github.com/spring-projects/spring-ai/issues/5134)).
- **Wrong tool chosen on ambiguous questions.** Almost always a description problem, not a model problem. Sharpen the descriptions before blaming the model.
- **Model fabricates a result instead of calling the tool.** Lower the temperature (this recipe uses 0.1) and make the system prompt explicit that answers must be grounded in tool output.
- **Parameter names lost at runtime.** If `@ToolParam` descriptions seem ignored, ensure you are compiling with parameter names retained. Spring Boot's Maven plugin handles this by default; a custom compiler config might not.

## Project structure

```
03-tool-calling/
├── pom.xml
├── .env.example
└── src/main/
    ├── java/com/triager/
    │   ├── ToolCallingApplication.java
    │   ├── controller/
    │   │   └── AssistantController.java
    │   ├── model/
    │   │   ├── AssistantRequest.java
    │   │   ├── PastBug.java
    │   │   └── ServiceStatus.java
    │   ├── service/
    │   │   ├── AssistantService.java     # wires tools into ChatClient
    │   │   └── BugRepository.java         # JdbcClient queries
    │   └── tools/
    │       └── BugTools.java              # the two @Tool methods
    └── resources/
        ├── application.yml
        ├── schema.sql                     # H2 tables
        └── data.sql                       # seed data
```
