package com.atlasrh.controller;

import com.atlasrh.dto.employee.SeniorityResponse;
import com.atlasrh.dto.leave.LeaveBalanceItem;
import com.atlasrh.dto.leave.LeaveRequestInput;
import com.atlasrh.dto.leave.LeaveRequestResponse;
import com.atlasrh.entity.LeaveStatus;
import com.atlasrh.service.EmployeeLeaveService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}")
public class EmployeeLeaveController {
    private final EmployeeLeaveService service;
    public EmployeeLeaveController(EmployeeLeaveService service) { this.service = service; }

    @Operation(summary = "List an employee's leave requests, optionally filtered by status")
    @GetMapping("/leave-requests")
    public List<LeaveRequestResponse> findRequests(@PathVariable Long employeeId, @RequestParam(required = false) LeaveStatus status) { return service.findRequests(employeeId, status); }

    @Operation(summary = "Create a leave request")
    @PostMapping("/leave-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestResponse create(@PathVariable Long employeeId, @Valid @RequestBody LeaveRequestInput request) {
        return service.create(employeeId, request);
    }

    @Operation(summary = "Cancel a pending leave request")
    @PatchMapping("/leave-requests/{requestId}/cancel")
    public LeaveRequestResponse cancel(@PathVariable Long employeeId, @PathVariable Long requestId) {
        return service.cancel(employeeId, requestId);
    }

    @Operation(summary = "Get the approved leave balance for a year")
    @GetMapping("/leave-balance")
    public List<LeaveBalanceItem> balance(@PathVariable Long employeeId, @RequestParam(required = false) Integer year) {
        return service.balance(employeeId, year == null ? LocalDate.now().getYear() : year);
    }

    @Operation(summary = "Calculate employee seniority from hire date")
    @GetMapping("/seniority")
    public SeniorityResponse seniority(@PathVariable Long employeeId) { return service.seniority(employeeId); }
}
