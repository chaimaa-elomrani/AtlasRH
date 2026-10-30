package com.atlasrh.mapper;

import com.atlasrh.dto.leave.LeaveRequestResponse;
import com.atlasrh.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "leaveTypeId", source = "leaveType.id")
    @Mapping(target = "leaveType", source = "leaveType.code")
    LeaveRequestResponse toResponse(LeaveRequest request);
}
