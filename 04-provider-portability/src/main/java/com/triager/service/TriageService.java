package com.triager.service;

import com.triager.model.TriagedReport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This service contains zero provider-specific code. It injects the generic
 * ChatClient.Builder and calls .entity(). Whether the model behind it is Gemini
 * or something reached through OpenRouter is decided entirely by which starter
 * is on the classpath and what is in the active profile's config. None of that
 * leaks into this class. That is the portability promise, demonstrated.
 */
@Service
public class TriageService {

    private final ChatClient chatClient;
    private final String activeModelLabel;

    public TriageService(
        ChatClient.Builder builder,
        @Value("${app.active-model-label:unknown}") String activeModelLabel
    ) {
        this.activeModelLabel = activeModelLabel;
        this.chatClient = builder
            .defaultSystem("""
                You are a bug report triager for a software engineering team.
                Given a bug report, classify it into a structured triage record.

                Rules:
                - severity: CRITICAL if data is lost or the app is unusable; HIGH for major
                  functionality broken; MEDIUM for degraded experience; LOW for cosmetic issues.
                - component: a short lowercase identifier like "auth", "billing", "search", "ui".
                - suggestedLabels: 2-4 short labels engineers would add to the ticket, each with
                  a confidence score between 0.0 and 1.0.
                - summary: one sentence, present tense, plain English.
                """)
            .build();
    }

    public TriagedReport triage(String bugReport) {
        return chatClient.prompt()
            .user(bugReport)
            .call()
            .entity(TriagedReport.class);
    }

    public String activeModelLabel() {
        return activeModelLabel;
    }
}
