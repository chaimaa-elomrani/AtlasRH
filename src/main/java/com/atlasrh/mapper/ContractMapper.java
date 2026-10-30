package com.atlasrh.mapper;

import com.atlasrh.dto.contract.ContractResponse;
import com.atlasrh.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractMapper {
    @Mapping(target = "employeeId", source = "employee.id")
    ContractResponse toResponse(Contract contract);
}
