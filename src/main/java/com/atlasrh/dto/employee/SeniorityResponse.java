package com.atlasrh.dto.employee;

import java.time.LocalDate;

public record SeniorityResponse(Long employeeId, LocalDate hireDate, int years, int months, int days) {}
