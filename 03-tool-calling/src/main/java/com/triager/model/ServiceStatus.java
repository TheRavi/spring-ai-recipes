package com.triager.model;

public record ServiceStatus(
    String component,
    String status,
    String detail
) {}
