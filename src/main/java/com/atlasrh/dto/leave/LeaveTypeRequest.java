package com.atlasrh.dto.leave;

import com.atlasrh.entity.LeaveTypeCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LeaveTypeRequest(@NotNull LeaveTypeCode code, @Min(0) int daysPerYear) {}
