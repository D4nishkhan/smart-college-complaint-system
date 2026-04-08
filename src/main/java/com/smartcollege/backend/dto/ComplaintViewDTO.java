package com.smartcollege.backend.dto;

import com.smartcollege.backend.entity.Complaint;

public class ComplaintViewDTO {

    private Long id;
    private String title;
    private String description;
    private Complaint.Status status;

    public ComplaintViewDTO(Complaint complaint) {
        this.id = complaint.getId();
        this.title = complaint.getTitle();
        this.description = complaint.getDescription();
        this.status = complaint.getStatus();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Complaint.Status getStatus() {
        return status;
    }
}
