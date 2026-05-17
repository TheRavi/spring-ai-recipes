package com.triager.controller;

import com.triager.model.TriageRequest;
import com.triager.model.TriagedReport;
import com.triager.service.TriageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
