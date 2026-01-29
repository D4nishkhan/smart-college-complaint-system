package com.smartcollege.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    public Complaint() {
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // getters & setters
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}

