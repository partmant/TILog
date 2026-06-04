package com.tilog.domain.report.controller;

import com.tilog.domain.report.dto.AiWeeklyReportResponse;
import com.tilog.domain.report.service.AiWeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * POST /api/members/{memberId}/weekly-reports
 *   ?weekStart=2026-05-26   (생략 시 이번 주 월요일로 자동 설정)
 *
 * 이미 생성된 리포트가 있으면 캐시된 결과를 반환하고,
 * 없으면 집계 → 코멘트 생성 → 저장 후 반환.
 *
 * NOTE: SecurityConfig에서 PREMIUM 이상 권한만 접근 가능하도록 설정 필요.
 */
@RestController
@RequestMapping("/api/members/{memberId}/weekly-reports")
@RequiredArgsConstructor
public class AiWeeklyReportController {

    private final AiWeeklyReportService aiWeeklyReportService;

    @PostMapping
    public ResponseEntity<AiWeeklyReportResponse> generate(
            @PathVariable Long memberId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart) {

        LocalDate resolved = weekStart != null ? weekStart : thisMonday();
        return ResponseEntity.ok(aiWeeklyReportService.generateReport(memberId, resolved));
    }

    private LocalDate thisMonday() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }
}