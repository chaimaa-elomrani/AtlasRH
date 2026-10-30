package com.atlasrh.controller;

import com.atlasrh.dto.department.DepartmentRequest;
import com.atlasrh.dto.department.DepartmentResponse;
import com.atlasrh.service.DepartmentService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService service;
    public DepartmentController(DepartmentService service) { this.service = service; }

    @Operation(summary = "List departments")
    @GetMapping public List<DepartmentResponse> findAll() { return service.findAll(); }
    @Operation(summary = "Get a department by ID")
    @GetMapping("/{id}") public DepartmentResponse findById(@PathVariable Long id) { return service.findById(id); }
    @Operation(summary = "Create a department; its name must be unique")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request) { return service.create(request); }
    @Operation(summary = "Update a department")
    @PutMapping("/{id}") public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) { return service.update(id, request); }
    @Operation(summary = "Delete an empty department")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
