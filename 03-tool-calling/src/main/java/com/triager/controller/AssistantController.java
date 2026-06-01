package com.triager.controller;

import com.triager.model.AssistantRequest;
import com.triager.service.AssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public String ask(@RequestBody AssistantRequest request) {
        return assistantService.ask(request.question());
    }
}
