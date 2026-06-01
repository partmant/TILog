package com.tilog.entity;

import com.tilog.dto.report.TechStackDistributionData;
import com.tilog.dto.report.WeeklySummaryData;
import com.tilog.entity.converter.TechStackDistributionDataConverter;
import com.tilog.entity.converter.WeeklySummaryDataConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_weekly_report")
@Getter
@NoArgsConstructor
public class AiWeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Convert(converter = WeeklySummaryDataConverter.class)
    @Column(name = "weekly_summary_data", columnDefinition = "TEXT")
    private WeeklySummaryData weeklySummaryData;

    @Convert(converter = TechStackDistributionDataConverter.class)
    @Column(name = "tech_stack_distribution_data", columnDefinition = "TEXT")
    private TechStackDistributionData techStackDistributionData;

    @Column(name = "rule_based_comment", columnDefinition = "TEXT")
    private String ruleBasedComment;

    @Column(name = "ai_analysis_comment", columnDefinition = "TEXT")
    private String aiAnalysisComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AiWeeklyReport(Member member, LocalDate weekStartDate, LocalDate weekEndDate,
                          WeeklySummaryData weeklySummaryData,
                          TechStackDistributionData techStackDistributionData,
                          String ruleBasedComment) {
        this.member = member;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.weeklySummaryData = weeklySummaryData;
        this.techStackDistributionData = techStackDistributionData;
        this.ruleBasedComment = ruleBasedComment;
        this.createdAt = LocalDateTime.now();
    }

    /** LLM 응답 수신 후 AI 코멘트만 업데이트 */
    public void applyAiAnalysis(String aiAnalysisComment) {
        this.aiAnalysisComment = aiAnalysisComment;
    }
}