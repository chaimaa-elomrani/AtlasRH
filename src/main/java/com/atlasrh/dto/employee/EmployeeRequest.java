package com.atlasrh.dto.employee;

import com.atlasrh.entity.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank @Size(max = 30) String matricule,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String poste,
        @NotNull @PastOrPresent LocalDate hireDate,
        @NotNull Long departmentId,
        Long managerId,
        EmployeeStatus status) {

    public EmployeeRequest(
            String matricule,
            String firstName,
            String lastName,
            String email,
            String poste,
            LocalDate hireDate,
            Long departmentId,
            Long managerId) {
        this(matricule, firstName, lastName, email, poste, hireDate, departmentId, managerId, null);
    }
}
