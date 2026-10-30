package com.atlasrh.repository;

import com.atlasrh.entity.LeaveRequest;
import com.atlasrh.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeIdOrderByStartDateDesc(Long employeeId);
    List<LeaveRequest> findByEmployeeManagerIdAndStatusOrderByStartDateAsc(Long managerId, LeaveStatus status);
}
