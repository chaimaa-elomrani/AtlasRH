package com.atlasrh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "leave_type", uniqueConstraints = @UniqueConstraint(name = "uk_leave_type_code", columnNames = "code"))
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private LeaveTypeCode code;

    @Min(0)
    @Column(name = "days_per_year", nullable = false)
    private int daysPerYear;

    public LeaveType() {}

    public Long getId() { return id; }
    public LeaveTypeCode getCode() { return code; }
    public void setCode(LeaveTypeCode code) { this.code = code; }
    public int getDaysPerYear() { return daysPerYear; }
    public void setDaysPerYear(int daysPerYear) { this.daysPerYear = daysPerYear; }
}
