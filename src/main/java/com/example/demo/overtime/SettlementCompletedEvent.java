package com.example.demo.overtime;

import java.math.BigDecimal;
import java.time.YearMonth;

public record SettlementCompletedEvent(Long workerId, YearMonth month, BigDecimal settledAmount) {}
