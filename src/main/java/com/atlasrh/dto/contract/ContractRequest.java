package com.atlasrh.dto.contract;

import com.atlasrh.entity.ContractType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractRequest(@NotNull ContractType type, @NotNull LocalDate startDate,
                              LocalDate endDate,
                              @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal grossSalary,
                              @NotNull Long employeeId) {}
