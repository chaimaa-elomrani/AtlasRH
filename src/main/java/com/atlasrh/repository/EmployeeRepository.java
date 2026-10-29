package com.atlasrh.repository;

import com.atlasrh.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByMatriculeIgnoreCase(String matricule);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMatriculeIgnoreCaseAndIdNot(String matricule, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByDepartmentId(Long departmentId);
    Page<Employee> findByMatriculeContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String matricule, String firstName, String lastName, String email, Pageable pageable);
}
