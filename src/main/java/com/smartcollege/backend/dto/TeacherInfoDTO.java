package com.smartcollege.backend.dto;

public record TeacherInfoDTO(
        Long id,
        String name,
        String employeeCode,
        Long departmentId,
        String departmentName
) {}