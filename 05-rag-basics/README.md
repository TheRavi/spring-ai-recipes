# 05 — RAG basics

Give the assistant a memory. Load a corpus of incident documents into a vector store, retrieve the relevant ones at query time, and let Gemini answer from them instead of from its training data.

This is the final recipe in the Spring AI Recipes series. It closes the loop: recipe 03 let the model query structured data through tools, and this one lets it read unstructured documents through retrieval.

Paired with the blog post: [RAG basics in Spring AI: retrieval, the two-starter trap, and when retrieval lies](https://ravibuilds.dev/blog/spring-ai-rag-basics)

## What RAG does here

The app ingests three past incident write-ups on startup, embeds them into an in-memory vector store, and answers questions by retrieving the most relevant chunks and feeding them to the model as grounding. Ask "why did the analytics export return an empty file?" and it retrieves the matching incident and answers from it, citing the incident number.

## The dependencies that trip everyone up

Every previous recipe used a single dependency, `spring-ai-starter-model-google-genai`, for chat. RAG needs four things, and Spring AI splits them into separate fine-grained artifacts that the chat starter does not pull in transitively:

```xml
<!-- chat: gives you a ChatModel (you already had this) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>

<!-- embeddings: gives you an EmbeddingModel to turn text into vectors -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai-embedding</artifactId>
</dependency>

<!-- vector store: gives you the VectorStore interface and SimpleVectorStore -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vector-store</artifactId>
</dependency>

<!-- advisors: gives you QuestionAnswerAdvisor for one-call RAG -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-advisors-vector-store</artifactId>
</dependency>
```

Three of these are easy to miss, and they fail in different ways:

- **Without the embedding starter**, the `SimpleVectorStore` bean can't construct, because there's no `EmbeddingModel` to inject. The app fails to start with a missing-bean error.
- **Without `spring-ai-vector-store`**, the code won't compile: `import org.springframework.ai.vectorstore cannot be resolved`.
- **Without `spring-ai-advisors-vector-store`**, the code won't compile either: `import org.springframework.ai.chat.client.advisor.vectorstore cannot be resolved`.

This is the real lesson of the recipe. Spring AI breaks RAG into small, composable artifacts (chat, embeddings, vector-store API, advisors) and you assemble the ones you need. Powerful once you know it, but the first RAG build almost always fails two or three times on missing dependencies before it boots. That is not you doing it wrong; it is the cost of the fine-grained design.

## Requirements

- Java 21+
- Maven (the included `mvnw` wrapper works)
- A free Gemini API key from [aistudio.google.com/apikey](https://aistudio.google.com/apikey). The same key covers both chat and embeddings.

## Run it

```bash
cp .env.example .env
# edit .env and paste in your real key

set -a && source .env && set +a
./mvnw spring-boot:run
```

On startup you'll see a line like `>> Ingested 3 documents into N chunks.` confirming the corpus is loaded.

## Ask it something

```bash
curl -X POST http://localhost:8080/ask \
  -H 'Content-Type: application/json' \
  -d '{"question": "Why did the analytics CSV export return an empty file, and how was it fixed?"}'
```

The model retrieves the relevant incident and answers from it, citing the incident number.

## See what retrieval actually pulled

This is the endpoint that teaches you how RAG really behaves. It runs retrieval only, no chat model, and shows you which chunks matched and with what similarity score:

```bash
curl "http://localhost:8080/retrieve?question=why%20did%20the%20export%20fail&topK=3"
```

Try a deliberately ambiguous query like "why did the export fail." The corpus has two incidents about exports failing: one in analytics (a query timeout) and one in billing (connection-pool contention). Watch which one ranks higher, and by how much. This is where you feel the difference between "retrieval works" and "retrieval returns the chunk you actually wanted."

## What can go wrong

- **"Google GenAI project-id must be set!" at startup.** The embedding auto-config needs its own `spring.ai.google.genai.embedding.api-key` property. The chat side authenticates with just its key, but the embedding side falls through to the Vertex AI auth path (which demands a GCP project-id) unless you give it an explicit api-key. Set `spring.ai.google.genai.embedding.api-key` to the same key as chat. This is the most confusing failure in the recipe, because chat works fine and only embeddings break.
- **Missing advisors artifact (compile error).** If you see `import org.springframework.ai.chat.client.advisor.vectorstore cannot be resolved`, you're missing `spring-ai-advisors-vector-store`, which provides `QuestionAnswerAdvisor`.
- **Missing vector-store artifact (compile error).** If you see `import org.springframework.ai.vectorstore cannot be resolved`, you're missing `spring-ai-vector-store`. The chat and embedding starters don't bring it in transitively.
- **Missing embedding starter (startup error).** No embedding starter means no `EmbeddingModel` bean means the vector store won't construct. Add `spring-ai-starter-model-google-genai-embedding`.
- **Retrieval returns the wrong-but-similar chunk.** When two documents share vocabulary (here, two different "export failed" incidents), the vector store can rank the wrong one first. The model then answers confidently from the wrong context. Use the `/retrieve` endpoint to see what was actually pulled before trusting an answer. This is not a bug in Spring AI; it is the nature of similarity search.
- **Chunk size changes everything.** `TokenTextSplitter` defaults are fine for short docs like these. For longer documents, chunks that are too large dilute relevance and chunks that are too small lose context. There is no universally correct size; it depends on your corpus.
- **SimpleVectorStore is for development only.** It holds everything in memory and is not persistent across restarts. Spring AI's docs are explicit that it is for testing. For production, swap in pgvector, Redis, or another store. Because your code talks to the `VectorStore` interface, that swap is a dependency and config change, not a code change.
- **No similarity threshold by default.** Without a threshold, the store always returns your top-K, even if the best match is barely relevant. For a query with no good answer in the corpus, you still get chunks back, and the model may answer from them. Set a threshold in `SearchRequest` to make retrieval return nothing when nothing is close enough.

## Project structure

```
05-rag-basics/
├── pom.xml                        # chat + embedding starters + vector-store
├── .env.example
└── src/main/
    ├── java/com/triager/
    │   ├── RagBasicsApplication.java
    │   ├── config/
    │   │   └── VectorStoreConfig.java     # SimpleVectorStore bean
    │   ├── controller/
    │   │   └── RagController.java          # /ask and /retrieve
    │   └── service/
    │       ├── IngestionService.java       # loads + chunks + embeds on startup
    │       └── RagService.java             # QuestionAnswerAdvisor + manual retrieval
    └── resources/
        ├── application.yml                 # chat + embedding config
        └── docs/
            ├── incident-2041-analytics-export.txt
            ├── incident-2088-billing-export.txt
            └── incident-1990-sso-redirect.txt
```
