package com.atlasrh.dto.leave;

import com.atlasrh.entity.LeaveTypeCode;

public record LeaveTypeResponse(Long id, LeaveTypeCode code, int daysPerYear) {}
