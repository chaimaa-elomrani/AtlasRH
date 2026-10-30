package com.atlasrh.repository;

import com.atlasrh.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("select e from Employee e where (:q is null or lower(e.matricule) like lower(concat('%', :q, '%')) or lower(e.firstName) like lower(concat('%', :q, '%')) or lower(e.lastName) like lower(concat('%', :q, '%')) or lower(e.email) like lower(concat('%', :q, '%')) or lower(e.department.name) like lower(concat('%', :q, '%'))) and (:departmentId is null or e.department.id = :departmentId)")
    Page<Employee> search(@Param("q") String query, @Param("departmentId") Long departmentId, Pageable pageable);
    Page<Employee> findByManagerId(Long managerId, Pageable pageable);
    boolean existsByMatriculeIgnoreCase(String matricule);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMatriculeIgnoreCaseAndIdNot(String matricule, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByDepartmentId(Long departmentId);
    Page<Employee> findByMatriculeContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String matricule, String firstName, String lastName, String email, Pageable pageable);
}
