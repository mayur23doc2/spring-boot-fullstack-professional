package com.example.demo.attendance;

import com.example.demo.common.BaseAuditableEntity;
import com.example.demo.site.Site;
import com.example.demo.worker.Worker;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs", indexes = {
        @Index(name = "idx_att_worker_created", columnList = "worker_id,createdAt"),
        @Index(name = "idx_att_open_clockout", columnList = "clockOutTime")
})
public class AttendanceLog extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(nullable = false)
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    @Column(precision = 8, scale = 2)
    private BigDecimal totalHoursWorked;
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    @Column(nullable = false)
    private boolean flagged = false;

    public Long getId() { return id; }
    public Worker getWorker() { return worker; }
    public void setWorker(Worker worker) { this.worker = worker; }
    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }
    public LocalDateTime getClockInTime() { return clockInTime; }
    public void setClockInTime(LocalDateTime clockInTime) { this.clockInTime = clockInTime; }
    public LocalDateTime getClockOutTime() { return clockOutTime; }
    public void setClockOutTime(LocalDateTime clockOutTime) { this.clockOutTime = clockOutTime; }
    public BigDecimal getTotalHoursWorked() { return totalHoursWorked; }
    public void setTotalHoursWorked(BigDecimal totalHoursWorked) { this.totalHoursWorked = totalHoursWorked; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
}
