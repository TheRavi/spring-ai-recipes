package com.triager.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = builder
            .defaultSystem("""
                You are a support engineer assistant. Answer the question using only
                the provided incident documents. If the documents do not contain the
                answer, say so plainly. Do not guess. Cite the incident number you
                used.
                """)
            .build();
    }

    /**
     * The clean path. QuestionAnswerAdvisor retrieves relevant chunks from the
     * vector store and injects them into the prompt automatically. This is the
     * one-call RAG experience.
     */
    public String ask(String question) {
        return chatClient.prompt()
            .user(question)
            .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
            .call()
            .content();
    }

    /**
     * The transparent path. Runs the same retrieval by hand so you can see
     * exactly which chunks came back and with what similarity, before the model
     * ever sees them. This is how you debug a wrong RAG answer: look at what was
     * retrieved, not just what was generated.
     */
    public RetrievalDebug debugRetrieval(String question, int topK) {
        List<Document> hits = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(topK)
                .build()
        );

        List<RetrievedChunk> chunks = hits.stream()
            .map(doc -> new RetrievedChunk(
                String.valueOf(doc.getMetadata().getOrDefault("source", "unknown")),
                doc.getScore() == null ? -1 : doc.getScore(),
                truncate(doc.getText(), 200)
            ))
            .toList();

        return new RetrievalDebug(question, chunks);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    public record RetrievedChunk(String source, double score, String preview) {}

    public record RetrievalDebug(String question, List<RetrievedChunk> retrieved) {}
}
