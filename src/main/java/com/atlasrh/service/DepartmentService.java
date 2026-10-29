package com.atlasrh.service;

import com.atlasrh.dto.department.DepartmentRequest;
import com.atlasrh.dto.department.DepartmentResponse;
import com.atlasrh.entity.Department;
import com.atlasrh.exception.BusinessConflictException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.repository.DepartmentRepository;
import com.atlasrh.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository departments;
    private final EmployeeRepository employees;

    public DepartmentService(DepartmentRepository departments, EmployeeRepository employees) {
        this.departments = departments;
        this.employees = employees;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() { return departments.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) { return toResponse(getDepartment(id)); }

    public DepartmentResponse create(DepartmentRequest request) {
        if (departments.existsByNameIgnoreCase(request.name().trim())) throw new BusinessConflictException("Department name already exists");
        Department department = new Department(request.name().trim(), request.description());
        return toResponse(departments.save(department));
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getDepartment(id);
        if (departments.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) throw new BusinessConflictException("Department name already exists");
        department.setName(request.name().trim());
        department.setDescription(request.description());
        return toResponse(department);
    }

    public void delete(Long id) {
        Department department = getDepartment(id);
        if (employees.existsByDepartmentId(id)) throw new BusinessConflictException("A department with employees cannot be deleted");
        departments.delete(department);
    }

    private Department getDepartment(Long id) {
        return departments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department " + id + " not found"));
    }
    private DepartmentResponse toResponse(Department d) { return new DepartmentResponse(d.getId(), d.getName(), d.getDescription()); }
}
