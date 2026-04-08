package com.smartcollege.backend.controller;

import com.smartcollege.backend.dto.ComplaintResponseDTO;
import com.smartcollege.backend.entity.Complaint;
import com.smartcollege.backend.entity.Department;
import com.smartcollege.backend.entity.Teacher;
import com.smartcollege.backend.exception.ApiException;
import com.smartcollege.backend.repository.ComplaintRepository;
import com.smartcollege.backend.repository.DepartmentRepository;
import com.smartcollege.backend.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/principal")
@CrossOrigin(origins = "*")
public class PrincipalController {

    private final ComplaintRepository complaintRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;

    @Value("${principal.username}")
    private String principalUsername;

    @Value("${principal.secret}")
    private String principalSecret;

    public PrincipalController(ComplaintRepository complaintRepository,
                               TeacherRepository teacherRepository,
                               DepartmentRepository departmentRepository) {
        this.complaintRepository = complaintRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
    }

    private void auth(String username, String secret) {
        if (username == null || secret == null ||
                !principalUsername.equals(username) ||
                !principalSecret.equals(secret)) {
            throw ApiException.unauthorized("Invalid principal credentials");
        }
    }

    // 1) Pending teacher requests
    @GetMapping("/teachers/pending")
    public ResponseEntity<?> pendingTeachers(
            @RequestHeader("X-PRINCIPAL-USERNAME") String username,
            @RequestHeader("X-PRINCIPAL-SECRET") String secret) {

        auth(username, secret);
        return ResponseEntity.ok(teacherRepository.findByVerifiedFalse());
    }

    // 2) Approved but department not assigned
    @GetMapping("/teachers/unassigned")
    public ResponseEntity<?> approvedButUnassignedTeachers(
            @RequestHeader("X-PRINCIPAL-USERNAME") String username,
            @RequestHeader("X-PRINCIPAL-SECRET") String secret) {

        auth(username, secret);
        return ResponseEntity.ok(teacherRepository.findByVerifiedTrueAndDepartmentIsNull());
    }

    // ✅ NEW: Principal can see all complaints (student + department joined)
    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintResponseDTO>> allComplaints(
            @RequestHeader("X-PRINCIPAL-USERNAME") String username,
            @RequestHeader("X-PRINCIPAL-SECRET") String secret) {

        auth(username, secret);

        return ResponseEntity.ok(
                complaintRepository.findAllWithStudentAndDepartment()
                        .stream()
                        .map(ComplaintResponseDTO::new)
                        .toList()
        );
    }

    // 3) Approve / Reject teacher
    @PutMapping("/teachers/{teacherId}/approve")
    public ResponseEntity<String> approveTeacher(
            @RequestHeader("X-PRINCIPAL-USERNAME") String username,
            @RequestHeader("X-PRINCIPAL-SECRET") String secret,
            @PathVariable Long teacherId,
            @RequestParam boolean approved) {

        auth(username, secret);

        Teacher t = teacherRepository.findById(teacherId)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));

        if (!approved) {
            teacherRepository.delete(t);
            return ResponseEntity.ok("Teacher rejected successfully");
        }

        t.setVerified(true);

        // employeeCode auto assign
        if (t.getEmployeeCode() == null || t.getEmployeeCode().isBlank()) {
            t.setEmployeeCode("EMP" + String.format("%05d", t.getId()));
        }

        teacherRepository.save(t);

        return ResponseEntity.ok("Teacher approved successfully (ID: " + t.getId() +
                ", EmployeeCode: " + t.getEmployeeCode() + ")");
    }

    // 4) Assign department to teacher
    @PutMapping("/teachers/{teacherId}/department/{deptId}")
    public ResponseEntity<String> assignDepartment(
            @RequestHeader("X-PRINCIPAL-USERNAME") String username,
            @RequestHeader("X-PRINCIPAL-SECRET") String secret,
            @PathVariable Long teacherId,
            @PathVariable Long deptId) {

        auth(username, secret);

        Teacher t = teacherRepository.findById(teacherId)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));

        Department d = departmentRepository.findById(deptId)
                .orElseThrow(() -> ApiException.notFound("Department not found"));

        t.setDepartment(d);
        teacherRepository.save(t);

        return ResponseEntity.ok("Department assigned successfully");
    }

    // 5) Complaint status update
    @PutMapping("/complaints/{complaintId}/status")
    public ResponseEntity<String> updateComplaintStatus(
            @RequestHeader("X-PRINCIPAL-USERNAME") String username,
            @RequestHeader("X-PRINCIPAL-SECRET") String secret,
            @PathVariable Long complaintId,
            @RequestParam Complaint.Status status) {

        auth(username, secret);

        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> ApiException.notFound("Complaint not found"));

        c.setStatus(status);
        complaintRepository.save(c);

        return ResponseEntity.ok("Complaint status updated to " + status);
    }
}