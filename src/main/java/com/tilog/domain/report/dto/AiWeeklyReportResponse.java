package com.tilog.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** GET /api/members/{memberId}/weekly-report 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWeeklyReportResponse {

    private Long reportId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    private WeeklySummaryData weeklySummary;
    private TechStackDistributionData techStackDistribution;

    private String ruleBasedComment;
    private String aiAnalysisComment;

    private LocalDateTime createdAt;
}