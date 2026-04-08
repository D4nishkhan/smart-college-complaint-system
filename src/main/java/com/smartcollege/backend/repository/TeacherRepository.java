package com.smartcollege.backend.repository;

import com.smartcollege.backend.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmail(String email);

    // ✅ better: case-insensitive login
    Optional<Teacher> findByEmailIgnoreCase(String email);

    List<Teacher> findByVerifiedFalse();

    // ✅ for principal "approved but dept not assigned" table
    List<Teacher> findByVerifiedTrueAndDepartmentIsNull();

    Optional<Teacher> findByApiKey(String apiKey);
}