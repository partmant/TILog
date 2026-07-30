package com.tilog.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** Gemini가 반환하는 구조화된 AI 분석 결과 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnalysisResult {

    @JsonProperty("weekly_persona")
    private WeeklyPersona weeklyPersona;

    @JsonProperty("deep_tech_analysis")
    private DeepTechAnalysis deepTechAnalysis;

    @JsonProperty("career_alignment_audit")
    private CareerAlignmentAudit careerAlignmentAudit;

    @JsonProperty("practical_portfolio_advice")
    private PracticalPortfolioAdvice practicalPortfolioAdvice;

    @JsonProperty("next_week_roadmap")
    private NextWeekRoadmap nextWeekRoadmap;

    @JsonProperty("mentor_cheering_message")
    private String mentorCheeringMessage;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeeklyPersona {
        private String title;
        @JsonProperty("total_evaluation")
        private String totalEvaluation;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeepTechAnalysis {
        @JsonProperty("focus_area")
        private String focusArea;
        @JsonProperty("intensity_review")
        private String intensityReview;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CareerAlignmentAudit {
        /** BALANCED / BIAS_WARNING / INITIAL_STAGE */
        private String status;
        @JsonProperty("audit_comment")
        private String auditComment;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PracticalPortfolioAdvice {
        @JsonProperty("resume_keyword")
        private String resumeKeyword;
        @JsonProperty("interview_question")
        private String interviewQuestion;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NextWeekRoadmap {
        @JsonProperty("action_item")
        private String actionItem;
        @JsonProperty("recommended_tech_stacks")
        private List<RecommendedTechStack> recommendedTechStacks;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendedTechStack {
        @JsonProperty("tech_name")
        private String techName;
        private String reason;
    }
}