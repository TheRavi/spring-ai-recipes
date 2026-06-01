package com.triager.service;

import com.triager.tools.BugTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private final ChatClient chatClient;
    private final BugTools bugTools;

    public AssistantService(ChatClient.Builder builder, BugTools bugTools) {
        this.bugTools = bugTools;
        this.chatClient = builder
            .defaultSystem("""
                You are a support engineering assistant for a software team.
                You have two tools available:
                  - findSimilarBugs: searches the history of past bug reports
                  - getServiceStatus: checks the current live status of a component

                Decide which tool (or tools) a question needs. A question about whether
                something is broken *right now* needs the live status. A question about
                whether an issue has been *seen before* needs the bug history. A question
                that asks both ("is export down, and have we hit this before?") needs both.

                If neither tool is relevant, just answer directly. Keep answers concise
                and grounded in what the tools return. Do not invent bug IDs or statuses.
                """)
            .build();
    }

    public String ask(String question) {
        return chatClient.prompt()
            .user(question)
            .tools(bugTools)
            .call()
            .content();
    }
}
