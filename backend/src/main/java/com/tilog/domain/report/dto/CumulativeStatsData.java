package com.tilog.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/** cumulative_data 컬럼에 JSON으로 저장되는 누적 통계 스냅샷 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CumulativeStatsData {

    private int totalPosts;
    private int totalLearningMinutes;

    /** key: "BACKEND" | "FRONTEND" | "SECURITY" | "CS" | "OTHER", value: 비율(%) */
    private Map<String, Integer> categoryDistribution;

    /** key: 태그 이름, value: 전체 사용 횟수 */
    private Map<String, Integer> tagTotals;

    /** key: "EASY" | "NORMAL" | "HARD", value: 전체 게시글 수 */
    private Map<String, Integer> difficultyDistribution;
}