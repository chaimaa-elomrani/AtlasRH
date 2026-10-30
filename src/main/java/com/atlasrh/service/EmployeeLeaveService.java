package com.atlasrh.service;

import com.atlasrh.dto.employee.SeniorityResponse;
import com.atlasrh.dto.leave.LeaveBalanceItem;
import com.atlasrh.dto.leave.LeaveRequestInput;
import com.atlasrh.dto.leave.LeaveDecisionInput;
import com.atlasrh.dto.leave.LeaveRequestResponse;
import com.atlasrh.entity.*;
import com.atlasrh.exception.BusinessConflictException;
import com.atlasrh.exception.InvalidRequestException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.repository.EmployeeRepository;
import com.atlasrh.repository.LeaveRequestRepository;
import com.atlasrh.repository.LeaveTypeRepository;
import com.atlasrh.mapper.LeaveRequestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class EmployeeLeaveService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeLeaveService.class);
    private final EmployeeRepository employees;
    private final LeaveTypeRepository types;
    private final LeaveRequestRepository requests;
    private final LeaveRequestMapper mapper;

    public EmployeeLeaveService(EmployeeRepository employees, LeaveTypeRepository types, LeaveRequestRepository requests, LeaveRequestMapper mapper) {
        this.employees = employees;
        this.types = types;
        this.requests = requests;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findRequests(Long employeeId) {
        requireEmployee(employeeId);
        return requests.findByEmployeeIdOrderByStartDateDesc(employeeId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findRequests(Long employeeId, LeaveStatus status) {
        return findRequests(employeeId).stream().filter(r -> status == null || r.status() == status).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findPendingForManager(Long managerId) {
        requireEmployee(managerId);
        return requests.findByEmployeeManagerIdAndStatusOrderByStartDateAsc(managerId, LeaveStatus.EN_ATTENTE)
                .stream().map(this::toResponse).toList();
    }

    public LeaveRequestResponse decide(Long managerId, Long requestId, LeaveDecisionInput input) {
        requireEmployee(managerId);
        LeaveRequest request = requests.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request " + requestId + " not found"));
        if (request.getEmployee().getManager() == null || !request.getEmployee().getManager().getId().equals(managerId)) {
            throw new ResourceNotFoundException("Leave request " + requestId + " not found for manager " + managerId);
        }
        if (request.getStatus() != LeaveStatus.EN_ATTENTE) throw new BusinessConflictException("Only pending leave requests can be decided");
        String comment = input.comment() == null || input.comment().isBlank() ? null : input.comment().trim();
        if (!input.approved() && comment == null) throw new InvalidRequestException("A comment is required to refuse a leave request");
        request.setStatus(input.approved() ? LeaveStatus.APPROUVEE : LeaveStatus.REFUSEE);
        request.setDecisionComment(comment);
        log.info("Leave request {} decided by manager {} with status {}", requestId, managerId, request.getStatus());
        return toResponse(request);
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
        request.setStatus(LeaveStatus.EN_ATTENTE);
        log.info("Leave request submitted for employee {} and leave type {}", employeeId, type.getCode());
        return toResponse(requests.save(request));
    }

    public LeaveRequestResponse cancel(Long employeeId, Long requestId) {
        requireEmployee(employeeId);
        LeaveRequest request = requests.findById(requestId).orElseThrow(() -> new ResourceNotFoundException("Leave request " + requestId + " not found"));
        if (!request.getEmployee().getId().equals(employeeId)) throw new ResourceNotFoundException("Leave request " + requestId + " not found for employee " + employeeId);
        if (request.getStatus() != LeaveStatus.EN_ATTENTE) throw new BusinessConflictException("Only pending leave requests can be cancelled");
        request.setStatus(LeaveStatus.ANNULEE);
        log.info("Leave request {} cancelled by employee {}", requestId, employeeId);
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceItem> balance(Long employeeId, int year) {
        requireEmployee(employeeId);
        List<LeaveRequest> employeeRequests = requests.findByEmployeeIdOrderByStartDateDesc(employeeId);
        return types.findAll().stream().map(type -> {
            int approved = employeeRequests.stream()
                    .filter(r -> r.getLeaveType().getId().equals(type.getId()) && r.getStatus() == LeaveStatus.APPROUVEE)
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
    private boolean reservesDates(LeaveRequest request) { return request.getStatus() == LeaveStatus.EN_ATTENTE || request.getStatus() == LeaveStatus.APPROUVEE; }
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

    private LeaveRequestResponse toResponse(LeaveRequest request) { return mapper.toResponse(request); }
}
