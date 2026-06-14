package com.triager.controller;

import com.triager.service.RagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    public record AskRequest(String question) {}

    /**
     * Full RAG: retrieve relevant chunks and let the model answer from them.
     */
    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody AskRequest request) {
        return Map.of("answer", ragService.ask(request.question()));
    }

    /**
     * Retrieval only. Shows which chunks the vector store returned for a query
     * and their similarity scores, without involving the chat model. Use this
     * to debug why an answer came out the way it did.
     */
    @GetMapping("/retrieve")
    public RagService.RetrievalDebug retrieve(
        @RequestParam String question,
        @RequestParam(defaultValue = "3") int topK
    ) {
        return ragService.debugRetrieval(question, topK);
    }
}
