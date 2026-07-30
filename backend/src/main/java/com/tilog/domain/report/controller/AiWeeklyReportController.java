package com.tilog.domain.report.controller;

import com.tilog.domain.report.dto.AiWeeklyReportResponse;
import com.tilog.domain.report.service.AiWeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/members/{memberId}/weekly-reports")
@RequiredArgsConstructor
public class AiWeeklyReportController {

    private final AiWeeklyReportService aiWeeklyReportService;

    /**
     * GET /api/members/{memberId}/weekly-reports/latest
     * 가장 최근 리포트 조회. 없으면 204 No Content.
     */
    @GetMapping("/latest")
    public ResponseEntity<AiWeeklyReportResponse> getLatest(@PathVariable Long memberId) {
        return aiWeeklyReportService.findLatestReport(memberId)
                .map(report -> ResponseEntity.ok(report))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * GET /api/members/{memberId}/weekly-reports?weekStart=2026-05-26
     * 특정 주 리포트 조회. weekStart 생략 시 이번 주 월요일. 없으면 204 No Content.
     */
    @GetMapping
    public ResponseEntity<AiWeeklyReportResponse> getReport(
            @PathVariable Long memberId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart) {

        LocalDate resolved = weekStart != null ? weekStart : thisMonday();
        return aiWeeklyReportService.findReport(memberId, resolved)
                .map(report -> ResponseEntity.ok(report))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * POST /api/members/{memberId}/weekly-reports?weekStart=2026-05-26
     * 리포트 생성. 이미 존재하면 캐시된 결과 반환. weekStart 생략 시 지난 주 월요일.
     * 완료된 주(이번 주 월요일 이전)만 생성 가능 — 진행 중인 주는 400 반환.
     * NOTE: SecurityConfig에서 PREMIUM 이상 권한만 접근 가능하도록 설정 필요.
     */
    @PostMapping
    public ResponseEntity<AiWeeklyReportResponse> generate(
            @PathVariable Long memberId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart) {

        LocalDate resolved = weekStart != null ? weekStart : lastMonday();
        if (!resolved.isBefore(thisMonday())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(aiWeeklyReportService.generateReport(memberId, resolved));
    }

    private LocalDate lastMonday() {
        return thisMonday().minusWeeks(1);
    }

    private LocalDate thisMonday() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }
}