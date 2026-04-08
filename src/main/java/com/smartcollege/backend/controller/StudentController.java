package com.smartcollege.backend.controller;

import com.smartcollege.backend.dto.*;
import com.smartcollege.backend.entity.Complaint;
import com.smartcollege.backend.entity.Department;
import com.smartcollege.backend.entity.Student;
import com.smartcollege.backend.exception.ApiException;
import com.smartcollege.backend.repository.ComplaintRepository;
import com.smartcollege.backend.repository.DepartmentRepository;
import com.smartcollege.backend.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintRepository complaintRepository;

    public StudentController(StudentRepository studentRepository,
                             DepartmentRepository departmentRepository,
                             ComplaintRepository complaintRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.complaintRepository = complaintRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<StudentRegisterResponseDTO> register(@RequestBody StudentRegisterRequest req) {

        if (req.email() == null || req.email().isBlank())
            throw ApiException.badRequest("email is required");
        if (req.password() == null || req.password().isBlank())
            throw ApiException.badRequest("password is required");
        if (req.departmentId() == null)
            throw ApiException.badRequest("departmentId is required");

        if (studentRepository.existsByEmail(req.email())) {
            throw ApiException.badRequest("TRY WITH YOUR OWN EMAIL THIS EMAIL IS ALREADY SIGNED-UP");
        }

        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> ApiException.notFound("Department not found"));

        Student s = new Student();
        s.setName(req.name());
        s.setEmail(req.email());
        s.setPassword(req.password());
        s.setDepartment(dept);

        // apiKey guarantee entity @PrePersist bhi karta hai, but ok to set
        s.setApiKey(UUID.randomUUID().toString());

        Student saved = studentRepository.save(s);
        return ResponseEntity.ok(new StudentRegisterResponseDTO(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody StudentLoginRequest req) {

        Student s = studentRepository.findByEmail(req.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));

        if (!s.getPassword().equals(req.password())) {
            throw ApiException.unauthorized("Invalid credentials");
        }

        if (s.getApiKey() == null || s.getApiKey().isBlank()) {
            s.setApiKey(UUID.randomUUID().toString());
            studentRepository.save(s);
        }

        return ResponseEntity.ok(new AuthResponseDTO(s.getId(), s.getName(), s.getApiKey()));
    }

    @PostMapping("/me/complaints")
    public ResponseEntity<ComplaintResponseDTO> createComplaint(
            @RequestHeader(value = "X-STUDENT-KEY", required = false) String apiKey,
            @RequestBody ComplaintCreateRequest req) {

        if (apiKey == null || apiKey.isBlank())
            throw ApiException.unauthorized("Missing X-STUDENT-KEY");

        Student s = studentRepository.findByApiKey(apiKey)
                .orElseThrow(() -> ApiException.unauthorized("Invalid student key"));

        if (req.title() == null || req.title().isBlank())
            throw ApiException.badRequest("title is required");
        if (req.description() == null || req.description().isBlank())
            throw ApiException.badRequest("description is required");

        Complaint c = new Complaint();
        c.setStudent(s);
        c.setTitle(req.title());
        c.setDescription(req.description());

        Complaint saved = complaintRepository.save(c);

        // join-fetch reload (avoid lazy issues)
        Complaint full = complaintRepository.findByIdWithStudentAndDepartment(saved.getId())
                .orElseThrow(() -> ApiException.notFound("Complaint not found after save"));

        return ResponseEntity.ok(new ComplaintResponseDTO(full));
    }

    @GetMapping("/me/complaints")
    public ResponseEntity<?> myComplaints(
            @RequestHeader(value = "X-STUDENT-KEY", required = false) String apiKey) {

        if (apiKey == null || apiKey.isBlank())
            throw ApiException.unauthorized("Missing X-STUDENT-KEY");

        studentRepository.findByApiKey(apiKey)
                .orElseThrow(() -> ApiException.unauthorized("Invalid student key"));

        return ResponseEntity.ok(
                complaintRepository.findAllForStudentKey(apiKey)
                        .stream()
                        .map(ComplaintResponseDTO::new)
                        .toList()
        );
    }
}