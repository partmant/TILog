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

    @JsonProperty("this_week")
    private ThisWeekStats thisWeek;

    @JsonProperty("compared_to_last_week")
    private WeeklyComparison comparedToLastWeek;

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