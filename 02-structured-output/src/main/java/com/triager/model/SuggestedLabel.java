package com.triager.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"label", "confidence"})
public record SuggestedLabel(
    String label,
    double confidence
) {}
