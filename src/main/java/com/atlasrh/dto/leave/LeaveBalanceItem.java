package com.atlasrh.dto.leave;

import com.atlasrh.entity.LeaveTypeCode;

public record LeaveBalanceItem(Long leaveTypeId, LeaveTypeCode leaveType, int year,
                               int entitledDays, int approvedDays, int remainingDays) {}
