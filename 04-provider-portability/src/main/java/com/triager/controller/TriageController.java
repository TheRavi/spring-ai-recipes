package com.triager.controller;

import com.triager.model.TriageRequest;
import com.triager.model.TriagedReport;
import com.triager.service.TriageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TriageController {

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    @PostMapping("/triage")
    public TriagedReport triage(@RequestBody TriageRequest request) {
        return triageService.triage(request.bugReport());
    }

    /**
     * Confirms which provider/model is currently wired in. Hit this after
     * switching profiles to verify the swap actually took effect.
     */
    @GetMapping("/provider")
    public Map<String, String> provider() {
        return Map.of("activeModel", triageService.activeModelLabel());
    }
}
