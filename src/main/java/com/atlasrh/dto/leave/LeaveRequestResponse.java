package com.atlasrh.dto.leave;

import com.atlasrh.entity.LeaveStatus;
import com.atlasrh.entity.LeaveTypeCode;

import java.time.LocalDate;

public record LeaveRequestResponse(Long id, Long employeeId, Long leaveTypeId,
                                   LeaveTypeCode leaveType, LocalDate startDate, LocalDate endDate,
                                   int workingDays, String reason, LeaveStatus status,
                                   String decisionComment) {}
