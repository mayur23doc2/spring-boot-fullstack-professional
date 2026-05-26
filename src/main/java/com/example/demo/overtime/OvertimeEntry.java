package com.example.demo.overtime;

import com.example.demo.attendance.AttendanceLog;
import com.example.demo.common.BaseAuditableEntity;
import com.example.demo.worker.Worker;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "overtime_entries", indexes = {
        @Index(name = "idx_overtime_worker_date", columnList = "worker_id,date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_overtime_attendance", columnNames = "attendance_log_id")
})
public class OvertimeEntry extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_log_id", nullable = false)
    private AttendanceLog attendanceLog;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal overtimeHours;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal overtimeRateApplied;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    public Long getId() { return id; }
    public Worker getWorker() { return worker; }
    public void setWorker(Worker worker) { this.worker = worker; }
    public AttendanceLog getAttendanceLog() { return attendanceLog; }
    public void setAttendanceLog(AttendanceLog attendanceLog) { this.attendanceLog = attendanceLog; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public BigDecimal getOvertimeRateApplied() { return overtimeRateApplied; }
    public void setOvertimeRateApplied(BigDecimal overtimeRateApplied) { this.overtimeRateApplied = overtimeRateApplied; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public SettlementStatus getSettlementStatus() { return settlementStatus; }
    public void setSettlementStatus(SettlementStatus settlementStatus) { this.settlementStatus = settlementStatus; }
}
