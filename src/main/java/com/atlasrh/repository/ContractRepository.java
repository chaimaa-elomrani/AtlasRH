package com.atlasrh.repository;

import com.atlasrh.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByEmployeeIdOrderByStartDateDesc(Long employeeId);
    List<Contract> findByEmployeeId(Long employeeId);
    boolean existsByEmployeeIdAndEndDateIsNull(Long employeeId);
}
