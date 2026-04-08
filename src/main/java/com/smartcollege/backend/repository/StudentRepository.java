package com.smartcollege.backend.repository;

import com.smartcollege.backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Student> findByApiKey(String apiKey);

    List<Student> findByDepartment_Id(Long departmentId);

    // ✅ NEW: all students with department joined (no lazy error)
    @Query("""
        select s from Student s
        join fetch s.department d
        order by lower(d.name), lower(s.name)
    """)
    List<Student> findAllWithDepartment();
}