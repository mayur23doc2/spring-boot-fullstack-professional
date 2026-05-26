package com.example.demo.overtime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OvertimeDtos {
    public record DayBreakdown(LocalDate date, BigDecimal overtimeHours, BigDecimal amount, SettlementStatus status) {}
    public record OvertimeSummary(Long workerId, String month, BigDecimal totalOvertimeHours, BigDecimal totalPayout, SettlementStatus settlementStatus, List<DayBreakdown> breakdown) {}
    public record SettlementResponse(Long workerId, String month, BigDecimal totalSettledAmount, int entriesSettled) {}
}
