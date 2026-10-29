package com.atlasrh.dto.employee;

import java.time.LocalDate;

public record EmployeeResponse(Long id, String matricule, String firstName, String lastName,
                               String email, String poste, LocalDate hireDate,
                               Long departmentId, String departmentName, Long managerId) {}
