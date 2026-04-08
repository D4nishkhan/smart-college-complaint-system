package com.smartcollege.backend.dto;

import com.smartcollege.backend.entity.Student;

public class StudentViewDTO {
    public Long id;
    public String name;
    public String email;
    public Long departmentId;
    public String departmentName;

    public StudentViewDTO(Student s) {
        this.id = s.getId();
        this.name = s.getName();
        this.email = s.getEmail();
        if (s.getDepartment() != null) {
            this.departmentId = s.getDepartment().getId();
            this.departmentName = s.getDepartment().getName();
        }
    }
}