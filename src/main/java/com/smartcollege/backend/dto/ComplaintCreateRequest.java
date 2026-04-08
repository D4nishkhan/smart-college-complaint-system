package com.smartcollege.backend.dto;

public record ComplaintCreateRequest(
        String title,
        String description
) {}