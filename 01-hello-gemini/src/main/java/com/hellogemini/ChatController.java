package com.hellogemini;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
 
    private final ChatClient chatClient;
 
    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
 
    /// Sends the user message to Gemini and returns the response.
    ///
    /// @param message the user prompt; defaults to a brief self-introduction request
    /// @return a [ChatResponse] containing Gemini's reply
    @GetMapping("/chat")
    public ChatResponse chat(
            @RequestParam(defaultValue = "Hello, Gemini. Briefly introduce yourself.") String message) {
        var reply = chatClient
                .prompt()
                .user(message)
                .call()
                .content();
 
        return new ChatResponse(message, reply);
    }
 
    /// Response shape for the `/chat` endpoint.
    ///
    /// @param prompt the original user message echoed back for clarity
    /// @param reply  the text content returned by Gemini
    public record ChatResponse(String prompt, String reply) {}
}
