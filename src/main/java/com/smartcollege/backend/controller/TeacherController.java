package com.smartcollege.backend.controller;

import com.smartcollege.backend.dto.AuthResponseDTO;
import com.smartcollege.backend.dto.ComplaintResponseDTO;
import com.smartcollege.backend.dto.StudentViewDTO;
import com.smartcollege.backend.dto.TeacherInfoDTO;
import com.smartcollege.backend.dto.TeacherLoginRequest;
import com.smartcollege.backend.dto.TeacherRegisterRequest;
import com.smartcollege.backend.entity.Teacher;
import com.smartcollege.backend.exception.ApiException;
import com.smartcollege.backend.repository.ComplaintRepository;
import com.smartcollege.backend.repository.StudentRepository;
import com.smartcollege.backend.repository.TeacherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;

    public TeacherController(TeacherRepository teacherRepository,
                             ComplaintRepository complaintRepository,
                             StudentRepository studentRepository) {
        this.teacherRepository = teacherRepository;
        this.complaintRepository = complaintRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody TeacherRegisterRequest req) {

        if (req.email() == null || req.email().isBlank())
            throw ApiException.badRequest("email is required");
        if (req.password() == null || req.password().isBlank())
            throw ApiException.badRequest("password is required");

        if (teacherRepository.findByEmailIgnoreCase(req.email().trim()).isPresent()) {
            throw ApiException.badRequest("Email already exists");
        }

        Teacher t = new Teacher();
        t.setName(req.name());
        t.setEmail(req.email().trim());
        t.setPassword(req.password());

        t.setVerified(false);
        t.setDepartment(null);
        t.setApiKey(null);
        t.setEmployeeCode(null);

        teacherRepository.save(t);
        return ResponseEntity.ok("Request sent to principal for approval");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody TeacherLoginRequest req) {

        String email = (req.email() == null) ? "" : req.email().trim();

        Teacher t = teacherRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));

        if (!t.getPassword().equals(req.password())) {
            throw ApiException.unauthorized("Invalid credentials");
        }

        if (!t.isVerified()) {
            throw ApiException.forbidden("Your request is not approved by the Principal yet.");
        }
        if (t.getDepartment() == null) {
            throw ApiException.forbidden("Your department is not assigned by the Principal yet.");
        }

        if (t.getApiKey() == null) {
            t.setApiKey(UUID.randomUUID().toString());
            teacherRepository.save(t);
        }

        return ResponseEntity.ok(new AuthResponseDTO(t.getId(), t.getName(), t.getApiKey()));
    }

    private Teacher authTeacher(String apiKey) {
        if (apiKey == null || apiKey.isBlank())
            throw ApiException.unauthorized("Missing X-TEACHER-KEY");

        Teacher t = teacherRepository.findByApiKey(apiKey)
                .orElseThrow(() -> ApiException.unauthorized("Invalid teacher key"));

        if (!t.isVerified())
            throw ApiException.forbidden("Your request is not approved by the Principal yet.");
        if (t.getDepartment() == null)
            throw ApiException.forbidden("Your department is not assigned by the Principal yet.");

        return t;
    }

    // ✅ Teacher info (department etc.)
    @GetMapping("/me/info")
    public ResponseEntity<TeacherInfoDTO> myInfo(@RequestHeader("X-TEACHER-KEY") String apiKey) {
        Teacher t = authTeacher(apiKey);

        return ResponseEntity.ok(new TeacherInfoDTO(
                t.getId(),
                t.getName(),
                t.getEmployeeCode(),
                t.getDepartment().getId(),
                t.getDepartment().getName()
        ));
    }

    // ✅ NEW: Teacher can see ALL students (name + department)
    @GetMapping("/me/students/all")
    public ResponseEntity<?> allStudents(@RequestHeader("X-TEACHER-KEY") String apiKey) {
        authTeacher(apiKey); // only verified teachers

        return ResponseEntity.ok(
                studentRepository.findAllWithDepartment()
                        .stream()
                        .map(StudentViewDTO::new)
                        .toList()
        );
    }

    // ✅ Teacher sees ONLY dept complaints
    @GetMapping("/me/complaints")
    public ResponseEntity<?> myDepartmentComplaints(@RequestHeader("X-TEACHER-KEY") String apiKey) {
        Teacher t = authTeacher(apiKey);

        return ResponseEntity.ok(
                complaintRepository.findAllForDepartment(t.getDepartment().getId())
                        .stream()
                        .map(ComplaintResponseDTO::new)
                        .toList()
        );
    }
}