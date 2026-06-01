package com.triager.model;

public record PastBug(
    Long id,
    String component,
    String summary,
    String severity,
    String resolution
) {}
