package com.atlasrh.controller;

import com.atlasrh.dto.employee.EmployeeRequest;
import com.atlasrh.dto.employee.EmployeeResponse;
import com.atlasrh.exception.InvalidRequestException;
import com.atlasrh.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService service;
    public EmployeeController(EmployeeService service) { this.service = service; }

    @GetMapping public Page<EmployeeResponse> search(@RequestParam(required = false) String q,
                    @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        if (pageable.getPageSize() > 100) throw new InvalidRequestException("Page size cannot exceed 100");
        return service.search(q, pageable);
    }
    @GetMapping("/{id}") public EmployeeResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) { return service.create(request); }
    @PutMapping("/{id}") public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
