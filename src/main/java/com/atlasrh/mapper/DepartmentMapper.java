package com.atlasrh.mapper;

import com.atlasrh.dto.department.DepartmentResponse;
import com.atlasrh.entity.Department;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper { DepartmentResponse toResponse(Department department); }
