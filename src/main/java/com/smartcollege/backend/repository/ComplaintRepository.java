package com.smartcollege.backend.repository;

import com.smartcollege.backend.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Student own complaints using apiKey (secure) + joins (no lazy crash)
    @Query("""
        select c from Complaint c
        join fetch c.student s
        left join fetch s.department d
        where s.apiKey = :apiKey
        order by c.createdAt desc
    """)
    List<Complaint> findAllForStudentKey(@Param("apiKey") String apiKey);

    // Teacher department complaints + joins
    @Query("""
        select c from Complaint c
        join fetch c.student s
        join fetch s.department d
        where d.id = :departmentId
        order by c.createdAt desc
    """)
    List<Complaint> findAllForDepartment(@Param("departmentId") Long departmentId);

    // Reload single complaint with joins (POST create complaint ke baad 500 fix)
    @Query("""
        select c from Complaint c
        join fetch c.student s
        left join fetch s.department d
        where c.id = :complaintId
    """)
    Optional<Complaint> findByIdWithStudentAndDepartment(@Param("complaintId") Long complaintId);

    // ✅ DEBUG/Principal view (All complaints with student + department)
    @Query("""
        select c from Complaint c
        join fetch c.student s
        left join fetch s.department d
        order by c.createdAt desc
    """)
    List<Complaint> findAllWithStudentAndDepartment();
}