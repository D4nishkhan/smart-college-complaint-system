package com.smartcollege.backend.controller;

import com.smartcollege.backend.entity.Department;
import com.smartcollege.backend.entity.Student;
import com.smartcollege.backend.repository.DepartmentRepository;
import com.smartcollege.backend.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;

    public StudentController(StudentRepository studentRepository,
                             DepartmentRepository departmentRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
    }

    @PostMapping
    public Student addStudent(@RequestBody Student student) {
        Long deptId = student.getDepartment().getId();
        Department dept = departmentRepository.findById(deptId).orElseThrow();
        student.setDepartment(dept);
        return studentRepository.save(student);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}
