package com.example.demo.overtime;

import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {
    private final OvertimeService overtimeService;

    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    @GetMapping("/summary/{workerId}")
    public OvertimeDtos.OvertimeSummary summary(@PathVariable Long workerId, @RequestParam YearMonth month) {
        return overtimeService.summary(workerId, month);
    }

    @PostMapping("/settle/{workerId}")
    public OvertimeDtos.SettlementResponse settle(@PathVariable Long workerId, @RequestParam YearMonth month) {
        return overtimeService.settle(workerId, month);
    }
}
