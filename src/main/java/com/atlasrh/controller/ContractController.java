package com.atlasrh.controller;

import com.atlasrh.dto.contract.ContractRequest;
import com.atlasrh.dto.contract.ContractResponse;
import com.atlasrh.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    private final ContractService service;
    public ContractController(ContractService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ContractResponse create(@Valid @RequestBody ContractRequest request) { return service.create(request); }
    @GetMapping("/employee/{employeeId}") public List<ContractResponse> history(@PathVariable Long employeeId) { return service.history(employeeId); }
    @PatchMapping("/{id}/end") public ContractResponse end(@PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return service.end(id, endDate == null ? LocalDate.now() : endDate);
    }
}
