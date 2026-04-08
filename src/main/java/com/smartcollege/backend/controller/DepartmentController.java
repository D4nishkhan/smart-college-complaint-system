package com.smartcollege.backend.controller;

import com.smartcollege.backend.entity.Department;
import com.smartcollege.backend.exception.ApiException;
import com.smartcollege.backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @Value("${principal.username}")
    private String principalUsername;

    @Value("${principal.secret}")
    private String principalSecret;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // ✅ Anyone can view departments
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // ✅ Only Principal can add departments
    @PostMapping
    public Department addDepartment(
            @RequestHeader(value = "X-PRINCIPAL-USERNAME", required = false) String username,
            @RequestHeader(value = "X-PRINCIPAL-SECRET", required = false) String secret,
            @RequestBody Department department) {

        if (username == null || secret == null ||
                !principalUsername.equals(username) ||
                !principalSecret.equals(secret)) {
            throw ApiException.unauthorized("Only Principal can add departments");
        }

        if (department.getName() == null || department.getName().isBlank()) {
            throw ApiException.badRequest("Department name is required");
        }

        String cleanName = department.getName().trim();
        department.setName(cleanName);

        if (departmentRepository.existsByNameIgnoreCase(cleanName)) {
            throw ApiException.badRequest("Department already exists");
        }

        return departmentRepository.save(department);
    }
}