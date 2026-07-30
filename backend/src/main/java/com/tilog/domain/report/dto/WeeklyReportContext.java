package com.tilog.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** LLM API에 전달하는 컨텍스트 DTO. JSON 필드명은 snake_case로 고정 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportContext {

    @JsonProperty("user")
    private UserContext user;

    @JsonProperty("this_week")
    private ThisWeekStats thisWeek;

    @JsonProperty("compared_to_last_week")
    private WeeklyComparison comparedToLastWeek;

    @JsonProperty("cumulative_stats")
    private CumulativeStats cumulativeStats;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContext {
        /** null 허용 — 미입력 시 Gemini 방어 모드 프롬프트 적용 */
        @JsonProperty("current_status")
        private String currentStatus;

        @JsonProperty("target_job")
        private String targetJob;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThisWeekStats {

        @JsonProperty("total_posts")
        private int totalPosts;

        @JsonProperty("total_learning_time_minutes")
        private int totalLearningTimeMinutes;

        /** key: "BACKEND" | "FRONTEND" | "SECURITY" | "CS" | "OTHER", value: 비율(%) */
        @JsonProperty("category_distribution")
        private Map<String, Integer> categoryDistribution;

        /** key: 태그 이름, value: 게시글 수 */
        @JsonProperty("tag_distribution")
        private Map<String, Integer> tagDistribution;

        /** key: "EASY" | "NORMAL" | "HARD", value: 게시글 수 */
        @JsonProperty("difficulty_distribution")
        private Map<String, Integer> difficultyDistribution;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CumulativeStats {

        @JsonProperty("total_posts")
        private int totalPosts;

        @JsonProperty("total_learning_minutes")
        private int totalLearningMinutes;

        /** key: "BACKEND" | "FRONTEND" | "SECURITY" | "CS" | "OTHER", value: 비율(%) */
        @JsonProperty("category_distribution")
        private Map<String, Integer> categoryDistribution;

        /** key: 태그 이름, value: 전체 사용 횟수 */
        @JsonProperty("tag_totals")
        private Map<String, Integer> tagTotals;

        /** key: "EASY" | "NORMAL" | "HARD", value: 전체 게시글 수 */
        @JsonProperty("difficulty_distribution")
        private Map<String, Integer> difficultyDistribution;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyComparison {

        /** 양수=증가, 음수=감소 */
        @JsonProperty("post_count_change_percent")
        private int postCountChangePercent;

        @JsonProperty("learning_time_change_percent")
        private int learningTimeChangePercent;

        /** 이번 주에 처음 등장한 태그 이름 목록 */
        @JsonProperty("new_tags_tried")
        private List<String> newTagsTried;
    }
}