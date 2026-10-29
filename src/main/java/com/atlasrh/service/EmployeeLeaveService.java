package com.atlasrh.service;

import com.atlasrh.dto.employee.SeniorityResponse;
import com.atlasrh.dto.leave.LeaveBalanceItem;
import com.atlasrh.dto.leave.LeaveRequestInput;
import com.atlasrh.dto.leave.LeaveRequestResponse;
import com.atlasrh.entity.*;
import com.atlasrh.exception.BusinessConflictException;
import com.atlasrh.exception.InvalidRequestException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.repository.EmployeeRepository;
import com.atlasrh.repository.LeaveRequestRepository;
import com.atlasrh.repository.LeaveTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EmployeeLeaveService {
    private final EmployeeRepository employees;
    private final LeaveTypeRepository types;
    private final LeaveRequestRepository requests;

    public EmployeeLeaveService(EmployeeRepository employees, LeaveTypeRepository types, LeaveRequestRepository requests) {
        this.employees = employees;
        this.types = types;
        this.requests = requests;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findRequests(Long employeeId) {
        requireEmployee(employeeId);
        return requests.findByEmployeeIdOrderByStartDateDesc(employeeId).stream().map(this::toResponse).toList();
    }

    public LeaveRequestResponse create(Long employeeId, LeaveRequestInput input) {
        Employee employee = requireEmployee(employeeId);
        LeaveType type = types.findById(input.leaveTypeId()).orElseThrow(() -> new ResourceNotFoundException("Leave type " + input.leaveTypeId() + " not found"));
        LocalDate today = LocalDate.now();
        if (input.startDate().isBefore(today)) throw new InvalidRequestException("Leave cannot start in the past");
        if (input.endDate().isBefore(input.startDate())) throw new InvalidRequestException("End date cannot be before start date");
        int workingDays = workingDays(input.startDate(), input.endDate());
        if (workingDays == 0) throw new InvalidRequestException("Leave must include at least one working day");

        List<LeaveRequest> existing = requests.findByEmployeeIdOrderByStartDateDesc(employeeId);
        boolean overlaps = existing.stream().filter(this::reservesDates)
                .anyMatch(r -> !input.startDate().isAfter(r.getEndDate()) && !input.endDate().isBefore(r.getStartDate()));
        if (overlaps) throw new BusinessConflictException("Leave dates overlap an existing pending or approved request");
        ensureAvailable(type, input.startDate(), input.endDate(), existing);

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(type);
        request.setStartDate(input.startDate());
        request.setEndDate(input.endDate());
        request.setWorkingDays(workingDays);
        request.setReason(input.reason() == null || input.reason().isBlank() ? null : input.reason().trim());
        request.setStatus(LeaveStatus.PENDING);
        return toResponse(requests.save(request));
    }

    public LeaveRequestResponse cancel(Long employeeId, Long requestId) {
        requireEmployee(employeeId);
        LeaveRequest request = requests.findById(requestId).orElseThrow(() -> new ResourceNotFoundException("Leave request " + requestId + " not found"));
        if (!request.getEmployee().getId().equals(employeeId)) throw new ResourceNotFoundException("Leave request " + requestId + " not found for employee " + employeeId);
        if (request.getStatus() != LeaveStatus.PENDING) throw new BusinessConflictException("Only pending leave requests can be cancelled");
        request.setStatus(LeaveStatus.CANCELLED);
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceItem> balance(Long employeeId, int year) {
        requireEmployee(employeeId);
        List<LeaveRequest> employeeRequests = requests.findByEmployeeIdOrderByStartDateDesc(employeeId);
        return types.findAll().stream().map(type -> {
            int approved = employeeRequests.stream()
                    .filter(r -> r.getLeaveType().getId().equals(type.getId()) && r.getStatus() == LeaveStatus.APPROVED)
                    .mapToInt(r -> daysInYear(r.getStartDate(), r.getEndDate(), year)).sum();
            return new LeaveBalanceItem(type.getId(), type.getCode(), year, type.getDaysPerYear(), approved,
                    Math.max(0, type.getDaysPerYear() - approved));
        }).toList();
    }

    @Transactional(readOnly = true)
    public SeniorityResponse seniority(Long employeeId) {
        Employee employee = requireEmployee(employeeId);
        Period period = Period.between(employee.getHireDate(), LocalDate.now());
        return new SeniorityResponse(employeeId, employee.getHireDate(), period.getYears(), period.getMonths(), period.getDays());
    }

    private void ensureAvailable(LeaveType type, LocalDate start, LocalDate end, List<LeaveRequest> existing) {
        for (int year = start.getYear(); year <= end.getYear(); year++) {
            final int requestedYear = year;
            int requestedDays = daysInYear(start, end, year);
            int reservedDays = existing.stream()
                    .filter(r -> r.getLeaveType().getId().equals(type.getId()) && reservesBalance(r))
                    .mapToInt(r -> daysInYear(r.getStartDate(), r.getEndDate(), requestedYear)).sum();
            if (reservedDays + requestedDays > type.getDaysPerYear()) {
                throw new BusinessConflictException("Leave request exceeds the available " + type.getCode() + " balance for " + year);
            }
        }
    }

    private Employee requireEmployee(Long id) { return employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee " + id + " not found")); }
    private boolean reservesDates(LeaveRequest request) { return request.getStatus() == LeaveStatus.PENDING || request.getStatus() == LeaveStatus.APPROVED; }
    private boolean reservesBalance(LeaveRequest request) { return reservesDates(request); }

    private int daysInYear(LocalDate start, LocalDate end, int year) {
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        LocalDate clippedStart = start.isAfter(yearStart) ? start : yearStart;
        LocalDate clippedEnd = end.isBefore(yearEnd) ? end : yearEnd;
        return clippedEnd.isBefore(clippedStart) ? 0 : workingDays(clippedStart, clippedEnd);
    }

    private int workingDays(LocalDate start, LocalDate end) {
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        long weekdays = totalDays / 7 * 5;
        long remainder = totalDays % 7;
        for (int i = 0; i < remainder; i++) {
            DayOfWeek day = start.plusDays(totalDays / 7 * 7 + i).getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) weekdays++;
        }
        return Math.toIntExact(weekdays);
    }

    private LeaveRequestResponse toResponse(LeaveRequest request) {
        return new LeaveRequestResponse(request.getId(), request.getEmployee().getId(), request.getLeaveType().getId(),
                request.getLeaveType().getCode(), request.getStartDate(), request.getEndDate(), request.getWorkingDays(),
                request.getReason(), request.getStatus(), request.getDecisionComment());
    }
}
