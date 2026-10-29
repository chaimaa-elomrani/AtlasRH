package com.atlasrh.repository;

import com.atlasrh.entity.LeaveType;
import com.atlasrh.entity.LeaveTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    boolean existsByCode(LeaveTypeCode code);
    boolean existsByCodeAndIdNot(LeaveTypeCode code, Long id);
}
