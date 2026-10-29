package com.atlasrh.dto.contract;

import com.atlasrh.entity.ContractType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractResponse(Long id, ContractType type, LocalDate startDate, LocalDate endDate,
                               BigDecimal grossSalary, Long employeeId) {}
