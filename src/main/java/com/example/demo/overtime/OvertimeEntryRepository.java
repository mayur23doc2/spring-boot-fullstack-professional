package com.example.demo.overtime;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OvertimeEntryRepository extends JpaRepository<OvertimeEntry, Long> {
    List<OvertimeEntry> findByWorkerIdAndDateBetweenOrderByDateAsc(Long workerId, LocalDate from, LocalDate to);
}
