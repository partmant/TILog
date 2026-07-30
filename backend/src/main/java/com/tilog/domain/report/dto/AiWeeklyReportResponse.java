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
    private CumulativeStatsData cumulativeData;

    private String ruleBasedComment;
    /** DB 저장 원본 JSON (하위 호환용) */
    private String aiAnalysisComment;
    /** aiAnalysisComment를 서버에서 미리 파싱한 구조체 — 프론트엔드는 이 필드를 사용 */
    private AiAnalysisResult parsedAiAnalysis;
    /** 유저의 현재 신분 enum 코드 (JOB_SEEKER / STUDENT / EMPLOYED / CAREER_CHANGE / FREELANCER / null) — 프론트 포폴 섹션 타이틀 분기용 */
    private String currentStatus;

    private LocalDateTime createdAt;
}