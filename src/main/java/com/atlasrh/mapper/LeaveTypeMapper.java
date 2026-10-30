package com.atlasrh.mapper;

import com.atlasrh.dto.leave.LeaveTypeResponse;
import com.atlasrh.entity.LeaveType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LeaveTypeMapper { LeaveTypeResponse toResponse(LeaveType type); }
