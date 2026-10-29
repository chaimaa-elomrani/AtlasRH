package com.atlasrh.dto.leave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveRequestInput(@NotNull Long leaveTypeId, @NotNull LocalDate startDate,
                                @NotNull LocalDate endDate, @Size(max = 1000) String reason) {}
