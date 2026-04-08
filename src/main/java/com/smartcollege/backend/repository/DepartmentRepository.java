package com.smartcollege.backend.repository;

import com.smartcollege.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // ✅ duplicate name avoid (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // ✅ optional: UI me clean list (sorted)
    @Query("select d from Department d order by lower(d.name) asc")
    List<Department> findAllOrderByName();
}