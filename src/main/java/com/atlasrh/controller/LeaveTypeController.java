package com.atlasrh.controller;

import com.atlasrh.dto.leave.LeaveTypeRequest;
import com.atlasrh.dto.leave.LeaveTypeResponse;
import com.atlasrh.service.LeaveTypeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {
    private final LeaveTypeService service;
    public LeaveTypeController(LeaveTypeService service) {
        this.service = service;
    }

    @Operation(summary = "Consulter les types de congé et leur nombre annuel de jours")
    @GetMapping
    public List<LeaveTypeResponse> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Create a leave type")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveTypeResponse create(@Valid @RequestBody LeaveTypeRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Modifier le nombre annuel de jours de congé")
    @PutMapping("/{id}")
    public LeaveTypeResponse update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequest request) {
        return service.update(id, request);
    }
}
