package com.smartcollege.backend.dto;

public record TeacherLoginRequest(
        String email,
        String password
) {}