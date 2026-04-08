package com.smartcollege.backend.dto;

import com.smartcollege.backend.entity.Complaint;

import java.time.LocalDateTime;

public class ComplaintResponseDTO {
    public Long id;
    public String title;
    public String description;
    public String status;
    public LocalDateTime createdAt;

    public Long studentId;
    public String studentName;

    public Long departmentId;
    public String departmentName;

    public ComplaintResponseDTO(Complaint c) {
        this.id = c.getId();
        this.title = c.getTitle();
        this.description = c.getDescription();
        this.status = c.getStatus().name();
        this.createdAt = c.getCreatedAt();

        if (c.getStudent() != null) {
            this.studentId = c.getStudent().getId();
            this.studentName = c.getStudent().getName();

            if (c.getStudent().getDepartment() != null) {
                this.departmentId = c.getStudent().getDepartment().getId();
                this.departmentName = c.getStudent().getDepartment().getName();
            }
        }
    }
}