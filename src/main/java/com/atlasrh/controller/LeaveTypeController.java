package com.atlasrh.controller;

import com.atlasrh.dto.leave.LeaveTypeRequest;
import com.atlasrh.dto.leave.LeaveTypeResponse;
import com.atlasrh.service.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {
    private final LeaveTypeService service;
    public LeaveTypeController(LeaveTypeService service) { this.service = service; }

    @GetMapping public List<LeaveTypeResponse> findAll() { return service.findAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public LeaveTypeResponse create(@Valid @RequestBody LeaveTypeRequest request) { return service.create(request); }
    @PutMapping("/{id}") public LeaveTypeResponse update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequest request) { return service.update(id, request); }
}
