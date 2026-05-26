package com.example.demo.attendance;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AttendanceDtos {
    public record ClockInRequest(@NotNull Long workerId, @NotNull Long siteId) {}
    public record ClockOutRequest(@NotNull Long workerId) {}
    public record ActiveAttendanceDto(Long workerId, String workerName, Long siteId, String siteName, LocalDateTime clockInTime) {}
    public record AttendanceLogDto(Long id, Long workerId, String workerName, Long siteId, String siteName, LocalDateTime clockInTime, LocalDateTime clockOutTime, BigDecimal totalHoursWorked, BigDecimal overtimeHours, boolean flagged) {}
}
