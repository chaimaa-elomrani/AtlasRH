package com.atlasrh.service;

import com.atlasrh.dto.leave.LeaveDecisionInput;
import com.atlasrh.entity.Employee;
import com.atlasrh.entity.LeaveRequest;
import com.atlasrh.entity.LeaveStatus;
import com.atlasrh.entity.LeaveType;
import com.atlasrh.mapper.LeaveRequestMapper;
import com.atlasrh.exception.BusinessConflictException;
import com.atlasrh.exception.InvalidRequestException;
import com.atlasrh.repository.EmployeeRepository;
import com.atlasrh.repository.LeaveRequestRepository;
import com.atlasrh.repository.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeLeaveServiceTest {
    @Mock EmployeeRepository employees;
    @Mock LeaveTypeRepository types;
    @Mock LeaveRequestRepository requests;
    @Mock LeaveRequestMapper mapper;

    private EmployeeLeaveService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeLeaveService(employees, types, requests, mapper);
    }

    private void stubCreateDependencies() {
        Employee employee = mock(Employee.class);
        LeaveType annual = mock(LeaveType.class);
        lenient().when(annual.getDaysPerYear()).thenReturn(22);
        when(employees.findById(10L)).thenReturn(Optional.of(employee));
        when(types.findById(1L)).thenReturn(Optional.of(annual));
    }

    @Test
    void countsWeekdaysInclusivelyAndCreatesPendingRequest() {
        stubCreateDependencies();
        LocalDate monday = nextMonday();
        when(requests.findByEmployeeIdOrderByStartDateDesc(10L)).thenReturn(List.of());
        when(requests.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(10L, new com.atlasrh.dto.leave.LeaveRequestInput(1L, monday, monday.plusDays(4), "Test"));

        ArgumentCaptor<LeaveRequest> saved = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(requests).save(saved.capture());
        assertEquals(5, saved.getValue().getWorkingDays());
        assertEquals(LeaveStatus.EN_ATTENTE, saved.getValue().getStatus());
    }

    @Test
    void refusesCreationWhenDatesOverlapAnExistingPendingRequest() {
        stubCreateDependencies();
        LocalDate monday = nextMonday();
        LeaveRequest existing = mock(LeaveRequest.class);
        when(existing.getStatus()).thenReturn(LeaveStatus.EN_ATTENTE);
        when(existing.getStartDate()).thenReturn(monday);
        when(existing.getEndDate()).thenReturn(monday.plusDays(1));
        when(requests.findByEmployeeIdOrderByStartDateDesc(10L)).thenReturn(List.of(existing));

        assertThrows(BusinessConflictException.class, () -> service.create(10L,
                new com.atlasrh.dto.leave.LeaveRequestInput(1L, monday.plusDays(1), monday.plusDays(2), "Overlap")));
        verify(requests, never()).save(any(LeaveRequest.class));
    }

    @Test
    void managerMustCommentWhenRefusing() {
        Employee manager = mock(Employee.class);
        LeaveRequest request = mock(LeaveRequest.class);
        when(employees.findById(20L)).thenReturn(Optional.of(manager));
        when(requests.findById(30L)).thenReturn(Optional.of(request));
        Employee report = mock(Employee.class);
        when(request.getEmployee()).thenReturn(report);
        when(report.getManager()).thenReturn(manager);
        when(manager.getId()).thenReturn(20L);
        when(request.getStatus()).thenReturn(LeaveStatus.EN_ATTENTE);

        assertThrows(InvalidRequestException.class, () -> service.decide(20L, 30L, new LeaveDecisionInput(false, " ")));
        verify(request, never()).setStatus(LeaveStatus.REFUSEE);
    }

    private LocalDate nextMonday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) date = date.plusDays(1);
        return date;
    }
}
