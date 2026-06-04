package com.tilog.domain.report.service;

import com.tilog.domain.report.dto.TechStackDistributionData;
import com.tilog.domain.report.dto.WeeklySummaryData;

import java.util.List;
import java.util.Map;

public class RuleBasedCommentGenerator {

    private static final int SIGNIFICANT_THRESHOLD_PCT = 10;

    private RuleBasedCommentGenerator() {}

    /**
     * @param thisWeek  이번 주 요약
     * @param techStack 이번 주 기술 스택 분포
     * @param lastWeek  지난주 요약 (첫 주면 null)
     * @param newTags   이번 주 처음 등장한 태그 목록
     */
    public static String generate(WeeklySummaryData thisWeek,
                                  TechStackDistributionData techStack,
                                  WeeklySummaryData lastWeek,
                                  List<String> newTags) {
        if (thisWeek.getTotalPosts() == 0) {
            return "이번 주 작성한 TIL이 없어요. 다음 주엔 꼭 기록해봐요!";
        }

        StringBuilder sb = new StringBuilder();

        // 칭찬 문장 — 유의미하게 늘었을 때만 맨 앞에
        String praise = buildPraise(thisWeek, lastWeek);
        if (praise != null) sb.append(praise).append(" ");

        // 규칙 1: 최다 카테고리 비중
        String category = buildCategoryComment(techStack);
        if (category != null) sb.append(category).append(" ");

        // 규칙 2: 지난주 대비 작성량
        sb.append(buildComparisonComment(thisWeek, lastWeek)).append(" ");

        // 규칙 3: 새로 시도한 태그 (있을 때만)
        if (newTags != null && !newTags.isEmpty()) {
            sb.append(buildNewTagsComment(newTags));
        }

        return sb.toString().trim();
    }

    // ===== 칭찬 =====

    private static String buildPraise(WeeklySummaryData thisWeek, WeeklySummaryData lastWeek) {
        if (lastWeek == null) return null;

        int postDiff = thisWeek.getTotalPosts() - lastWeek.getTotalPosts();
        int timeDiff = thisWeek.getTotalLearningTimeMinutes() - lastWeek.getTotalLearningTimeMinutes();

        double postPct = changePct(lastWeek.getTotalPosts(), postDiff);
        double timePct = changePct(lastWeek.getTotalLearningTimeMinutes(), timeDiff);

        boolean postUp = postDiff > 0 && postPct >= SIGNIFICANT_THRESHOLD_PCT;
        boolean timeUp = timeDiff > 0 && timePct >= SIGNIFICANT_THRESHOLD_PCT;

        if (!postUp && !timeUp) return null;

        // 더 많이 늘어난 쪽을 칭찬
        if (postUp && (!timeUp || postPct >= timePct)) {
            return String.format("지난주보다 게시글을 %d개 더 작성했어요, 이번 주 정말 열심히 하셨네요!", postDiff);
        } else {
            return String.format("지난주보다 %d분 더 학습했어요, 이번 주 정말 열심히 하셨네요!", timeDiff);
        }
    }

    /** 지난주가 0이면 어떤 증가든 무한대 증가율로 처리 */
    private static double changePct(int base, int diff) {
        if (base == 0) return diff > 0 ? Double.MAX_VALUE : 0.0;
        return (double) diff / base * 100.0;
    }

    // ===== 규칙 1 =====

    private static String buildCategoryComment(TechStackDistributionData techStack) {
        if (techStack == null
                || techStack.getCategories() == null
                || techStack.getCategories().isEmpty()) return null;

        return techStack.getCategories().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("이번 주 #%s 비중 %d%%.", e.getKey(), e.getValue()))
                .orElse(null);
    }

    // ===== 규칙 2 =====

    private static String buildComparisonComment(WeeklySummaryData thisWeek, WeeklySummaryData lastWeek) {
        if (lastWeek == null) return "첫 번째 주간 리포트예요!";

        int lastPosts = lastWeek.getTotalPosts();
        if (lastPosts == 0) {
            return String.format("총 %d개의 TIL을 작성했어요.", thisWeek.getTotalPosts());
        }

        int pct = (int) Math.round((double)(thisWeek.getTotalPosts() - lastPosts) / lastPosts * 100);
        if (pct > 0) return String.format("지난주 대비 작성량 +%d%% ↑.", pct);
        if (pct < 0) return String.format("지난주 대비 작성량 %d%% ↓.", pct);
        return "지난주와 동일한 작성량이에요.";
    }

    // ===== 규칙 3 =====

    private static String buildNewTagsComment(List<String> newTags) {
        return "이번 주 새로 시도한 기술: " + String.join(", ", newTags) + ".";
    }
}