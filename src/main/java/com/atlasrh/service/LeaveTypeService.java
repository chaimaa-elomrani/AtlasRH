package com.atlasrh.service;

import com.atlasrh.dto.leave.LeaveTypeRequest;
import com.atlasrh.dto.leave.LeaveTypeResponse;
import com.atlasrh.entity.LeaveType;
import com.atlasrh.exception.BusinessConflictException;
import com.atlasrh.exception.InvalidRequestException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.repository.LeaveTypeRepository;
import com.atlasrh.mapper.LeaveTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeaveTypeService {
    private final LeaveTypeRepository types;
    private final LeaveTypeMapper mapper;
    public LeaveTypeService(LeaveTypeRepository types, LeaveTypeMapper mapper) { this.types = types; this.mapper = mapper; }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> findAll() { return types.findAll().stream().map(this::toResponse).toList(); }

    public LeaveTypeResponse create(LeaveTypeRequest request) {
        if (types.existsByCode(request.code())) throw new BusinessConflictException("Leave type already exists");
        LeaveType type = new LeaveType();
        type.setCode(request.code());
        type.setDaysPerYear(request.daysPerYear());
        return toResponse(types.save(type));
    }

    public LeaveTypeResponse update(Long id, LeaveTypeRequest request) {
        LeaveType type = types.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave type " + id + " not found"));
        if (request.code() != type.getCode()) throw new InvalidRequestException("Leave type code cannot be changed after creation");
        if (types.existsByCodeAndIdNot(request.code(), id)) throw new BusinessConflictException("Leave type already exists");
        type.setDaysPerYear(request.daysPerYear());
        return toResponse(type);
    }

    private LeaveTypeResponse toResponse(LeaveType type) { return mapper.toResponse(type); }
}
