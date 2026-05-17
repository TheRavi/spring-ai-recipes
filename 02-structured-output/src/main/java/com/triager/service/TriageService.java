package com.triager.service;

import com.triager.model.TriagedReport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TriageService {

    private final ChatClient chatClient;

    public TriageService(ChatClient.Builder builder) {
        this.chatClient = builder
            .defaultSystem("""
                You are a bug report triager for a software engineering team.
                Given a bug report from a user or customer, classify it into a structured triage record.

                Rules:
                - severity: CRITICAL if data is lost or the app is unusable; HIGH for major functionality broken;
                  MEDIUM for degraded experience; LOW for cosmetic or minor issues.
                - component: a short lowercase identifier like "auth", "billing", "search", "ui", "api", "database".
                - suggestedLabels: 2-4 short labels engineers would add to the ticket. Each label has a
                  confidence score between 0.0 and 1.0 reflecting how sure you are it applies.
                - summary: one sentence, present tense, plain English. No marketing speak.
                """)
            .build();
    }

    public TriagedReport triage(String bugReport) {
        return chatClient.prompt()
            .user(bugReport)
            .call()
            .entity(TriagedReport.class);
    }
}
