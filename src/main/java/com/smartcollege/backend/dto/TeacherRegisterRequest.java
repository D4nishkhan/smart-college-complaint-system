package com.smartcollege.backend.dto;

public record TeacherRegisterRequest(
        String name,
        String email,
        String password
) {}