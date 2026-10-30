package com.atlasrh.controller;

import com.atlasrh.dto.leave.LeaveDecisionInput;
import com.atlasrh.dto.leave.LeaveRequestResponse;
import com.atlasrh.service.EmployeeLeaveService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/managers/{managerId}/leave-requests")
public class ManagerLeaveController {
    private final EmployeeLeaveService service;
    public ManagerLeaveController(EmployeeLeaveService service) { this.service = service; }

    @Operation(summary = "List pending leave requests from this manager's team")
    @GetMapping("/pending")
    public List<LeaveRequestResponse> pending(@PathVariable Long managerId) { return service.findPendingForManager(managerId); }

    @Operation(summary = "Approve or refuse a pending team request; refusal requires a comment")
    @PatchMapping("/{requestId}/decision")
    public LeaveRequestResponse decide(@PathVariable Long managerId, @PathVariable Long requestId,
                                       @Valid @RequestBody LeaveDecisionInput input) {
        return service.decide(managerId, requestId, input);
    }
}
