package com.smartcollege.backend.dto;

public record AuthResponseDTO(
        Long id,
        String name,
        String apiKey
) {}