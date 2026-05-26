package com.example.demo.overtime;

import com.example.demo.common.exception.ApiException;
import com.example.demo.worker.WorkerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class OvertimeService {
    private final OvertimeEntryRepository overtimeEntryRepository;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OvertimeService(OvertimeEntryRepository overtimeEntryRepository, WorkerRepository workerRepository, ApplicationEventPublisher eventPublisher) {
        this.overtimeEntryRepository = overtimeEntryRepository;
        this.workerRepository = workerRepository;
        this.eventPublisher = eventPublisher;
    }

    public OvertimeDtos.OvertimeSummary summary(Long workerId, YearMonth month) {
        assertWorker(workerId);
        List<OvertimeEntry> entries = monthEntries(workerId, month);
        BigDecimal totalHours = entries.stream().map(OvertimeEntry::getOvertimeHours).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPayout = entries.stream().map(OvertimeEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        SettlementStatus status = entries.stream().allMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED) ? SettlementStatus.SETTLED : SettlementStatus.PENDING;
        List<OvertimeDtos.DayBreakdown> breakdown = entries.stream().map(e -> new OvertimeDtos.DayBreakdown(e.getDate(), e.getOvertimeHours(), e.getAmount(), e.getSettlementStatus())).toList();
        return new OvertimeDtos.OvertimeSummary(workerId, month.toString(), totalHours, totalPayout, status, breakdown);
    }

    @Transactional
    public OvertimeDtos.SettlementResponse settle(Long workerId, YearMonth month) {
        assertWorker(workerId);
        if (month.equals(YearMonth.now())) {
            throw new ApiException("CURRENT_MONTH_SETTLEMENT_NOT_ALLOWED", "Cannot settle current month", HttpStatus.CONFLICT);
        }
        List<OvertimeEntry> entries = monthEntries(workerId, month);
        BigDecimal settled = BigDecimal.ZERO;
        int count = 0;
        for (OvertimeEntry entry : entries) {
            if (entry.getSettlementStatus() == SettlementStatus.SETTLED) continue;
            entry.setSettlementStatus(SettlementStatus.SETTLED);
            settled = settled.add(entry.getAmount());
            count++;
        }
        overtimeEntryRepository.saveAll(entries);
        eventPublisher.publishEvent(new SettlementCompletedEvent(workerId, month, settled));
        return new OvertimeDtos.SettlementResponse(workerId, month.toString(), settled, count);
    }

    private void assertWorker(Long workerId) {
        workerRepository.findById(workerId).orElseThrow(() -> new ApiException("WORKER_NOT_FOUND", "Worker not found", HttpStatus.NOT_FOUND));
    }

    private List<OvertimeEntry> monthEntries(Long workerId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return overtimeEntryRepository.findByWorkerIdAndDateBetweenOrderByDateAsc(workerId, from, to);
    }
}
