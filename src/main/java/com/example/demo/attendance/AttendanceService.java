package com.example.demo.attendance;

import com.example.demo.common.dto.PageResponse;
import com.example.demo.common.exception.ApiException;
import com.example.demo.overtime.OvertimeEntry;
import com.example.demo.overtime.OvertimeEntryRepository;
import com.example.demo.overtime.SettlementStatus;
import com.example.demo.site.Site;
import com.example.demo.site.SiteRepository;
import com.example.demo.worker.Worker;
import com.example.demo.worker.WorkerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {
    private static final BigDecimal STANDARD_SHIFT = BigDecimal.valueOf(8);
    private static final BigDecimal MONTHLY_OT_CAP = BigDecimal.valueOf(60);
    private final WorkerRepository workerRepository;
    private final SiteRepository siteRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final OvertimeEntryRepository overtimeEntryRepository;
    private final ActiveAttendanceCacheService cacheService;

    public AttendanceService(WorkerRepository workerRepository, SiteRepository siteRepository, AttendanceLogRepository attendanceLogRepository, OvertimeEntryRepository overtimeEntryRepository, ActiveAttendanceCacheService cacheService) {
        this.workerRepository = workerRepository;
        this.siteRepository = siteRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.overtimeEntryRepository = overtimeEntryRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public AttendanceDtos.AttendanceLogDto clockIn(AttendanceDtos.ClockInRequest request) {
        Worker worker = workerRepository.findById(request.workerId()).filter(Worker::isActive).orElseThrow(() -> new ApiException("WORKER_NOT_FOUND", "Worker not found or inactive", HttpStatus.NOT_FOUND));
        Site site = siteRepository.findById(request.siteId()).filter(Site::isActive).orElseThrow(() -> new ApiException("SITE_NOT_FOUND", "Site not found or inactive", HttpStatus.NOT_FOUND));
        attendanceLogRepository.findFirstByWorkerIdAndClockOutTimeIsNullOrderByClockInTimeDesc(worker.getId()).ifPresent(log -> {
            throw new ApiException("DUPLICATE_CLOCK_IN", "Worker is already clocked in at Site: " + log.getSite().getSiteName(), HttpStatus.CONFLICT);
        });
        AttendanceLog log = new AttendanceLog();
        log.setWorker(worker);
        log.setSite(site);
        log.setClockInTime(LocalDateTime.now());
        AttendanceLog saved = attendanceLogRepository.save(log);
        cacheService.put(new AttendanceDtos.ActiveAttendanceDto(worker.getId(), worker.getName(), site.getId(), site.getSiteName(), saved.getClockInTime()));
        return toDto(saved);
    }

    @Transactional
    public AttendanceDtos.AttendanceLogDto clockOut(AttendanceDtos.ClockOutRequest request) {
        AttendanceLog log = attendanceLogRepository.findFirstByWorkerIdAndClockOutTimeIsNullOrderByClockInTimeDesc(request.workerId())
                .orElseThrow(() -> new ApiException("ACTIVE_CLOCK_IN_NOT_FOUND", "Worker has no active clock-in", HttpStatus.CONFLICT));
        LocalDateTime out = LocalDateTime.now();
        BigDecimal totalHours = BigDecimal.valueOf(Duration.between(log.getClockInTime(), out).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal overtime = totalHours.subtract(STANDARD_SHIFT).max(BigDecimal.ZERO);
        log.setClockOutTime(out);
        log.setTotalHoursWorked(totalHours);
        log.setOvertimeHours(overtime);
        log.setFlagged(totalHours.compareTo(BigDecimal.valueOf(16)) > 0);
        AttendanceLog saved = attendanceLogRepository.save(log);
        if (overtime.compareTo(BigDecimal.ZERO) > 0) createOvertimeEntry(saved, overtime);
        cacheService.remove(request.workerId());
        return toDto(saved);
    }

    public List<AttendanceDtos.ActiveAttendanceDto> getActiveWorkers() {
        return cacheService.getAllActive();
    }

    public PageResponse<AttendanceDtos.AttendanceLogDto> getLogs(Long workerId, LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "clockInTime"));
        return PageResponse.from(attendanceLogRepository.findByWorkerIdAndClockInTimeBetween(workerId, from.atStartOfDay(), to.plusDays(1).atStartOfDay(), pageable).map(this::toDto));
    }

    private void createOvertimeEntry(AttendanceLog log, BigDecimal overtimeHours) {
        Worker worker = log.getWorker();
        LocalDate monthStart = log.getClockInTime().toLocalDate().withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        BigDecimal already = overtimeEntryRepository.findByWorkerIdAndDateBetweenOrderByDateAsc(worker.getId(), monthStart, monthEnd).stream()
                .map(OvertimeEntry::getOvertimeHours).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = MONTHLY_OT_CAP.subtract(already).max(BigDecimal.ZERO);
        BigDecimal capped = overtimeHours.min(remaining);
        BigDecimal amount = calculateAmount(worker.getDailyWageRate(), capped);
        OvertimeEntry entry = new OvertimeEntry();
        entry.setWorker(worker);
        entry.setAttendanceLog(log);
        entry.setDate(log.getClockInTime().toLocalDate());
        entry.setOvertimeHours(capped);
        entry.setOvertimeRateApplied(BigDecimal.valueOf(1.5));
        entry.setAmount(amount);
        entry.setSettlementStatus(SettlementStatus.PENDING);
        overtimeEntryRepository.save(entry);
    }

    private BigDecimal calculateAmount(BigDecimal dailyWage, BigDecimal overtimeHours) {
        if (overtimeHours.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal hourly = dailyWage.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        BigDecimal firstTwo = overtimeHours.min(BigDecimal.valueOf(2));
        BigDecimal beyond = overtimeHours.subtract(BigDecimal.valueOf(2)).max(BigDecimal.ZERO);
        return firstTwo.multiply(hourly).multiply(BigDecimal.valueOf(1.5))
                .add(beyond.multiply(hourly).multiply(BigDecimal.valueOf(2)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private AttendanceDtos.AttendanceLogDto toDto(AttendanceLog log) {
        return new AttendanceDtos.AttendanceLogDto(log.getId(), log.getWorker().getId(), log.getWorker().getName(), log.getSite().getId(), log.getSite().getSiteName(), log.getClockInTime(), log.getClockOutTime(), log.getTotalHoursWorked(), log.getOvertimeHours(), log.isFlagged());
    }
}
