package com.smartcollege.backend.dto;

public record StudentLoginRequest(
        String email,
        String password
) {}