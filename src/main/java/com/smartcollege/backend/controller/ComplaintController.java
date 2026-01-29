package com.smartcollege.backend.controller;

import com.smartcollege.backend.entity.Complaint;
import com.smartcollege.backend.entity.Department;
import com.smartcollege.backend.repository.ComplaintRepository;
import com.smartcollege.backend.repository.DepartmentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;

    public ComplaintController(ComplaintRepository complaintRepository,
                               DepartmentRepository departmentRepository) {
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
    }

    @PostMapping
    public Complaint addComplaint(@RequestBody Complaint complaint) {
        Long deptId = complaint.getDepartment().getId();
        Department dept = departmentRepository.findById(deptId).orElseThrow();
        complaint.setDepartment(dept);
        return complaintRepository.save(complaint);
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @PutMapping("/{id}/status")
    public Complaint updateStatus(@PathVariable Long id,
                                  @RequestParam String status) {
        Complaint complaint = complaintRepository.findById(id).orElseThrow();
        complaint.setStatus(status);
        return complaintRepository.save(complaint);
    }
}
