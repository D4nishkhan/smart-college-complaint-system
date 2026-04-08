package com.smartcollege.backend.service;

import com.smartcollege.backend.entity.Department;
import com.smartcollege.backend.entity.Teacher;
import com.smartcollege.backend.repository.DepartmentRepository;
import com.smartcollege.backend.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrincipleService {

    @Value("${principal.secret}")
    private String principalSecret;

    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;

    public PrincipleService(TeacherRepository teacherRepository,
                            DepartmentRepository departmentRepository) {
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<Teacher> getUnverifiedTeachers(String secret) {
        validateSecret(secret);
        return teacherRepository.findByVerifiedFalse();
    }

    public void verifyTeacher(Long teacherId, Long departmentId, String secret) {
        validateSecret(secret);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        teacher.setDepartment(department);
        teacher.setVerified(true);

        teacherRepository.save(teacher);
    }

    private void validateSecret(String secret) {
        if (!principalSecret.equals(secret)) {
            throw new RuntimeException("Invalid principal secret");
        }
    }
}
