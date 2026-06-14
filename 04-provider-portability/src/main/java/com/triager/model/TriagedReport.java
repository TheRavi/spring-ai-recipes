package com.triager.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"summary", "severity", "component", "suggestedLabels"})
public record TriagedReport(
    String summary,
    Severity severity,
    String component,
    List<SuggestedLabel> suggestedLabels
) {}
