package com.atlasrh.controller;

import com.atlasrh.dto.employee.SeniorityResponse;
import com.atlasrh.dto.leave.LeaveBalanceItem;
import com.atlasrh.dto.leave.LeaveRequestInput;
import com.atlasrh.dto.leave.LeaveRequestResponse;
import com.atlasrh.service.EmployeeLeaveService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees/{employeeId}")
public class EmployeeLeaveController {
    private final EmployeeLeaveService service;
    public EmployeeLeaveController(EmployeeLeaveService service) { this.service = service; }

    @GetMapping("/leave-requests")
    public List<LeaveRequestResponse> findRequests(@PathVariable Long employeeId) { return service.findRequests(employeeId); }

    @PostMapping("/leave-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestResponse create(@PathVariable Long employeeId, @Valid @RequestBody LeaveRequestInput request) {
        return service.create(employeeId, request);
    }

    @PatchMapping("/leave-requests/{requestId}/cancel")
    public LeaveRequestResponse cancel(@PathVariable Long employeeId, @PathVariable Long requestId) {
        return service.cancel(employeeId, requestId);
    }

    @GetMapping("/leave-balance")
    public List<LeaveBalanceItem> balance(@PathVariable Long employeeId, @RequestParam(required = false) Integer year) {
        return service.balance(employeeId, year == null ? LocalDate.now().getYear() : year);
    }

    @GetMapping("/seniority")
    public SeniorityResponse seniority(@PathVariable Long employeeId) { return service.seniority(employeeId); }
}
