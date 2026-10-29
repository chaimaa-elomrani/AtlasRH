package com.atlasrh.service;

import com.atlasrh.dto.contract.ContractRequest;
import com.atlasrh.dto.contract.ContractResponse;
import com.atlasrh.entity.Contract;
import com.atlasrh.entity.Employee;
import com.atlasrh.entity.ContractType;
import com.atlasrh.exception.BusinessConflictException;
import com.atlasrh.exception.InvalidRequestException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.repository.ContractRepository;
import com.atlasrh.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ContractService {
    private final ContractRepository contracts;
    private final EmployeeRepository employees;

    public ContractService(ContractRepository contracts, EmployeeRepository employees) {
        this.contracts = contracts;
        this.employees = employees;
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> history(Long employeeId) {
        requireEmployee(employeeId);
        return contracts.findByEmployeeIdOrderByStartDateDesc(employeeId).stream().map(this::toResponse).toList();
    }

    public ContractResponse create(ContractRequest request) {
        validateDates(request.type(), request.startDate(), request.endDate());
        Employee employee = requireEmployee(request.employeeId());
        LocalDate today = LocalDate.now();
        boolean hasCurrent = contracts.findByEmployeeId(employee.getId()).stream()
                .anyMatch(c -> !c.getStartDate().isAfter(today) && (c.getEndDate() == null || !c.getEndDate().isBefore(today)));
        if (hasCurrent) throw new BusinessConflictException("Employee already has a current contract");
        Contract contract = new Contract();
        contract.setType(request.type());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setGrossSalary(request.grossSalary());
        contract.setEmployee(employee);
        return toResponse(contracts.save(contract));
    }

    public ContractResponse end(Long id, LocalDate endDate) {
        Contract contract = contracts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract " + id + " not found"));
        if (contract.getEndDate() != null) throw new BusinessConflictException("Contract is already ended");
        if (endDate.isBefore(contract.getStartDate())) throw new InvalidRequestException("End date cannot be before start date");
        if (endDate.isAfter(LocalDate.now())) throw new InvalidRequestException("End date cannot be in the future");
        contract.setEndDate(endDate);
        return toResponse(contract);
    }

    private void validateDates(ContractType type, LocalDate start, LocalDate end) {
        if (end != null && end.isBefore(start)) throw new InvalidRequestException("End date cannot be before start date");
        if (type != ContractType.CDI && end == null) throw new InvalidRequestException("CDD and STAGE contracts require an end date");
    }

    private Employee requireEmployee(Long id) { return employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee " + id + " not found")); }
    private ContractResponse toResponse(Contract c) { return new ContractResponse(c.getId(), c.getType(), c.getStartDate(), c.getEndDate(), c.getGrossSalary(), c.getEmployee().getId()); }
}
