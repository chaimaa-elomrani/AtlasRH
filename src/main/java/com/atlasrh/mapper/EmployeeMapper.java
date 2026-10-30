package com.atlasrh.mapper;

import com.atlasrh.dto.employee.EmployeeResponse;
import com.atlasrh.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "managerId", source = "manager.id")
    EmployeeResponse toResponse(Employee employee);
}
