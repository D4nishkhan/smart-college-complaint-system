package com.smartcollege.backend.dto;

public record StudentRegisterRequest(
        String name,
        String email,
        String password,
        Long departmentId
) {}