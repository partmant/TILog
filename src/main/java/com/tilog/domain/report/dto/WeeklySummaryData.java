package com.tilog.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/** weekly_summary_data 컬럼에 JSON으로 저장되는 주간 요약 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryData {

    private int totalPosts;
    private int totalLearningTimeMinutes;

    /** key: "EASY" | "NORMAL" | "HARD", value: 게시글 수 */
    private Map<String, Integer> difficultyDistribution;
}