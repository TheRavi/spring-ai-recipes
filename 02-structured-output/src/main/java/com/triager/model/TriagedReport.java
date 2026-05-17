package com.triager.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * The structured triage result the LLM is asked to populate.
 * <p>
 * Spring AI's BeanOutputConverter derives a JSON schema from this record
 * and injects it into the prompt, then deserializes the model's response back
 * into an instance of this type.
 * <p>
 * The {@code @JsonPropertyOrder} annotation controls the field order in the
 * generated schema — useful because LLMs respond more reliably when reasoning
 * fields (like {@code summary}) come before classification fields.
 */
@JsonPropertyOrder({"summary", "severity", "component", "suggestedLabels"})
public record TriagedReport(
    String summary,
    Severity severity,
    String component,
    List<SuggestedLabel> suggestedLabels
) {}
