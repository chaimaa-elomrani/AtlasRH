package com.atlasrh.service;

import com.atlasrh.dto.employee.EmployeeRequest;
import com.atlasrh.dto.employee.EmployeeResponse;
import com.atlasrh.entity.Department;
import com.atlasrh.entity.Employee;
import com.atlasrh.exception.BusinessConflictException;
import com.atlasrh.exception.InvalidRequestException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.repository.DepartmentRepository;
import com.atlasrh.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {
    private final EmployeeRepository employees;
    private final DepartmentRepository departments;

    public EmployeeService(EmployeeRepository employees, DepartmentRepository departments) {
        this.employees = employees;
        this.departments = departments;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) return employees.findAll(pageable).map(this::toResponse);
        String q = query.trim();
        return employees.findByMatriculeContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, q, q, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) { return toResponse(getEmployee(id)); }

    public EmployeeResponse create(EmployeeRequest request) {
        ensureUnique(request, null);
        Employee employee = new Employee();
        apply(employee, request);
        return toResponse(employees.save(employee));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getEmployee(id);
        ensureUnique(request, id);
        apply(employee, request);
        return toResponse(employee);
    }

    public void delete(Long id) { employees.delete(getEmployee(id)); }

    private void ensureUnique(EmployeeRequest request, Long id) {
        boolean matriculeExists = id == null ? employees.existsByMatriculeIgnoreCase(request.matricule().trim()) : employees.existsByMatriculeIgnoreCaseAndIdNot(request.matricule().trim(), id);
        boolean emailExists = id == null ? employees.existsByEmailIgnoreCase(request.email().trim()) : employees.existsByEmailIgnoreCaseAndIdNot(request.email().trim(), id);
        if (matriculeExists) throw new BusinessConflictException("Employee matricule already exists");
        if (emailExists) throw new BusinessConflictException("Employee email already exists");
    }

    private void apply(Employee employee, EmployeeRequest request) {
        Department department = departments.findById(request.departmentId()).orElseThrow(() -> new ResourceNotFoundException("Department " + request.departmentId() + " not found"));
        Employee manager = null;
        if (request.managerId() != null) {
            if (request.managerId().equals(employee.getId())) throw new InvalidRequestException("An employee cannot be their own manager");
            manager = getEmployee(request.managerId());
        }
        employee.setMatricule(request.matricule().trim());
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setEmail(request.email().trim());
        employee.setPoste(request.poste().trim());
        employee.setHireDate(request.hireDate());
        employee.setDepartment(department);
        employee.setManager(manager);
    }

    private Employee getEmployee(Long id) { return employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee " + id + " not found")); }
    private EmployeeResponse toResponse(Employee e) { return new EmployeeResponse(e.getId(), e.getMatricule(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPoste(), e.getHireDate(), e.getDepartment().getId(), e.getDepartment().getName(), e.getManager() == null ? null : e.getManager().getId()); }
}
