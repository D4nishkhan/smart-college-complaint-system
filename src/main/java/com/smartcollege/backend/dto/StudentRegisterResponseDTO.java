package com.smartcollege.backend.dto;

import com.smartcollege.backend.entity.Student;

import java.time.LocalDateTime;

public record StudentRegisterResponseDTO(
        Long id,
        String name,
        String email,
        Long departmentId,
        String departmentName,
        LocalDateTime createdAt,
        String apiKey
) {
    public StudentRegisterResponseDTO(Student s) {
        this(
                s.getId(),
                s.getName(),
                s.getEmail(),
                s.getDepartment() != null ? s.getDepartment().getId() : null,
                s.getDepartment() != null ? s.getDepartment().getName() : null,
                s.getCreatedAt(),
                s.getApiKey()
        );
    }
}