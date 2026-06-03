package com.tilog.domain.report.service;

import com.tilog.domain.report.dto.AiWeeklyReportResponse;
import com.tilog.domain.report.dto.TechStackDistributionData;
import com.tilog.domain.report.dto.WeeklyReportContext;
import com.tilog.domain.report.dto.WeeklySummaryData;
import com.tilog.domain.report.entity.AiWeeklyReport;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.post.entity.Difficulty;
import com.tilog.domain.report.repository.AiWeeklyReportRepository;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.global.client.GeminiClient;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiWeeklyReportService {

    private final PostRepository postRepository;
    private final AiWeeklyReportRepository aiWeeklyReportRepository;
    private final EntityManager entityManager;
    private final GeminiClient geminiClient;

    /** 특정 주 리포트 조회 — 없으면 Optional.empty() */
    public Optional<AiWeeklyReportResponse> findReport(Long memberId, LocalDate weekStartDate) {
        return aiWeeklyReportRepository
                .findByMemberIdAndWeekStartDate(memberId, weekStartDate)
                .map(this::toResponse);
    }

    /** 가장 최근 리포트 조회 — 없으면 Optional.empty() */
    public Optional<AiWeeklyReportResponse> findLatestReport(Long memberId) {
        return aiWeeklyReportRepository
                .findTopByMemberIdOrderByWeekStartDateDesc(memberId)
                .map(this::toResponse);
    }

    /**
     * 주간 리포트 생성 — 이미 존재하면 캐시된 것을 반환.
     * weekStartDate는 해당 주 월요일 날짜.
     */
    @Transactional
    public AiWeeklyReportResponse generateReport(Long memberId, LocalDate weekStartDate) {
        return aiWeeklyReportRepository
                .findByMemberIdAndWeekStartDate(memberId, weekStartDate)
                .map(this::toResponse)
                .orElseGet(() -> createAndSave(memberId, weekStartDate));
    }

    // ===== 리포트 생성 =====

    private AiWeeklyReportResponse createAndSave(Long memberId, LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        LocalDateTime from = weekStartDate.atStartOfDay();
        LocalDateTime to   = weekEndDate.atTime(23, 59, 59);

        WeeklySummaryData summaryData = buildWeeklySummary(memberId, from, to);
        TechStackDistributionData techData = buildTechStackDistribution(memberId, from, to);

        // 이번 주 이전 전체 리포트 — 최신순 (쿼리 1번으로 직전 리포트 + 역대 누적 태그 모두 처리)
        List<AiWeeklyReport> pastReports = aiWeeklyReportRepository
                .findByMemberIdAndWeekStartDateBeforeOrderByWeekStartDateDesc(memberId, weekStartDate);

        AiWeeklyReport lastReport = pastReports.isEmpty() ? null : pastReports.get(0);
        WeeklySummaryData lastWeekSummary = lastReport != null ? lastReport.getWeeklySummaryData() : null;

        // 역대 모든 리포트에서 한 번이라도 쓴 태그 누적 집합
        Set<String> allHistoricalTags = pastReports.stream()
                .filter(r -> r.getTechStackDistributionData() != null
                          && r.getTechStackDistributionData().getTags() != null)
                .flatMap(r -> r.getTechStackDistributionData().getTags().keySet().stream())
                .collect(Collectors.toSet());

        List<String> newTags = findNewTags(techData.getTags(), allHistoricalTags);

        String ruleComment = RuleBasedCommentGenerator.generate(
                summaryData, techData, lastWeekSummary, newTags);

        // Gemini AI 분석 — 실패 시 CustomException 발생하여 저장되지 않음
        WeeklyReportContext context = buildContext(summaryData, techData, lastWeekSummary, newTags);
        String aiAnalysis = geminiClient.generateAiAnalysis(context);

        Member member = entityManager.getReference(Member.class, memberId);

        AiWeeklyReport report = new AiWeeklyReport(
                member, weekStartDate, weekEndDate, summaryData, techData, ruleComment);
        report.applyAiAnalysis(aiAnalysis);

        return toResponse(aiWeeklyReportRepository.save(report));
    }

    // ===== 집계 =====

    private WeeklySummaryData buildWeeklySummary(Long memberId, LocalDateTime from, LocalDateTime to) {
        Object[] row = postRepository.findWeeklySummary(memberId, from, to).get(0);
        int totalPosts = ((Long) row[0]).intValue();
        int totalTime  = ((Long) row[1]).intValue();

        Map<String, Integer> difficultyDist = new LinkedHashMap<>();
        postRepository.findDifficultyDistribution(memberId, from, to).forEach(r -> {
            Difficulty d = (Difficulty) r[0];
            if (d != null) difficultyDist.put(d.name(), ((Long) r[1]).intValue());
        });

        return WeeklySummaryData.builder()
                .totalPosts(totalPosts)
                .totalLearningTimeMinutes(totalTime)
                .difficultyDistribution(difficultyDist)
                .build();
    }

    private TechStackDistributionData buildTechStackDistribution(Long memberId,
                                                                  LocalDateTime from,
                                                                  LocalDateTime to) {
        List<Object[]> tagResult = postRepository.findTagDistribution(memberId, from, to);

        Map<String, Integer> tagCounts      = new LinkedHashMap<>();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        int total = 0;

        for (Object[] r : tagResult) {
            String tagName = (String) r[0];
            int count = ((Long) r[1]).intValue();
            tagCounts.put(tagName, count);
            categoryCounts.merge(TechStackCategoryMapper.categorize(tagName).name(), count, Integer::sum);
            total += count;
        }

        Map<String, Integer> categoryPct = new LinkedHashMap<>();
        if (total > 0) {
            final int finalTotal = total;
            categoryCounts.forEach((cat, cnt) ->
                    categoryPct.put(cat, (int) Math.round((double) cnt / finalTotal * 100)));
        }

        return TechStackDistributionData.builder()
                .categories(categoryPct)
                .tags(tagCounts)
                .build();
    }

    // ===== Gemini 컨텍스트 빌드 =====

    private WeeklyReportContext buildContext(WeeklySummaryData summaryData,
                                              TechStackDistributionData techData,
                                              WeeklySummaryData lastWeekSummary,
                                              List<String> newTags) {
        int postChange = 0;
        int timeChange = 0;
        if (lastWeekSummary != null && lastWeekSummary.getTotalPosts() > 0) {
            postChange = (int) Math.round(
                    (double)(summaryData.getTotalPosts() - lastWeekSummary.getTotalPosts())
                    / lastWeekSummary.getTotalPosts() * 100);
        }
        if (lastWeekSummary != null && lastWeekSummary.getTotalLearningTimeMinutes() > 0) {
            timeChange = (int) Math.round(
                    (double)(summaryData.getTotalLearningTimeMinutes() - lastWeekSummary.getTotalLearningTimeMinutes())
                    / lastWeekSummary.getTotalLearningTimeMinutes() * 100);
        }

        return WeeklyReportContext.builder()
                .thisWeek(WeeklyReportContext.ThisWeekStats.builder()
                        .totalPosts(summaryData.getTotalPosts())
                        .totalLearningTimeMinutes(summaryData.getTotalLearningTimeMinutes())
                        .categoryDistribution(techData.getCategories())
                        .tagDistribution(techData.getTags())
                        .difficultyDistribution(summaryData.getDifficultyDistribution())
                        .build())
                .comparedToLastWeek(WeeklyReportContext.WeeklyComparison.builder()
                        .postCountChangePercent(postChange)
                        .learningTimeChangePercent(timeChange)
                        .newTagsTried(newTags)
                        .build())
                .build();
    }

    // ===== 유틸 =====

    private List<String> findNewTags(Map<String, Integer> thisWeekTags, Set<String> lastWeekTags) {
        if (thisWeekTags == null) return Collections.emptyList();
        return thisWeekTags.keySet().stream()
                .filter(tag -> !lastWeekTags.contains(tag))
                .sorted()
                .toList();
    }

    private AiWeeklyReportResponse toResponse(AiWeeklyReport report) {
        return AiWeeklyReportResponse.builder()
                .reportId(report.getReportId())
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .weeklySummary(report.getWeeklySummaryData())
                .techStackDistribution(report.getTechStackDistributionData())
                .ruleBasedComment(report.getRuleBasedComment())
                .aiAnalysisComment(report.getAiAnalysisComment())
                .createdAt(report.getCreatedAt())
                .build();
    }
}