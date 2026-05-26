package com.example.demo.attendance;

import com.example.demo.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public AttendanceDtos.AttendanceLogDto clockIn(@Valid @RequestBody AttendanceDtos.ClockInRequest request) {
        return attendanceService.clockIn(request);
    }

    @PostMapping("/clock-out")
    public AttendanceDtos.AttendanceLogDto clockOut(@Valid @RequestBody AttendanceDtos.ClockOutRequest request) {
        return attendanceService.clockOut(request);
    }

    @GetMapping("/active")
    public List<AttendanceDtos.ActiveAttendanceDto> active() {
        return attendanceService.getActiveWorkers();
    }

    @GetMapping("/log")
    public PageResponse<AttendanceDtos.AttendanceLogDto> logs(@RequestParam Long workerId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return attendanceService.getLogs(workerId, from, to, page, size);
    }
}
