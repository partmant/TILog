package com.tilog.domain.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.post.entity.Difficulty;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.domain.report.dto.AiAnalysisResult;
import com.tilog.domain.report.dto.AiWeeklyReportResponse;
import com.tilog.domain.report.dto.CumulativeStatsData;
import com.tilog.domain.report.dto.TechStackDistributionData;
import com.tilog.domain.report.dto.WeeklyReportContext;
import com.tilog.domain.report.dto.WeeklySummaryData;
import com.tilog.domain.report.entity.AiWeeklyReport;
import com.tilog.domain.report.repository.AiWeeklyReportRepository;
import com.tilog.global.client.GeminiClient;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiWeeklyReportService {

    private final PostRepository postRepository;
    private final AiWeeklyReportRepository aiWeeklyReportRepository;
    private final MemberRepository memberRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        WeeklySummaryData summaryData    = buildWeeklySummary(memberId, from, to);
        TechStackDistributionData techData = buildTechStackDistribution(memberId, from, to);
        CumulativeStatsData cumulativeData = buildCumulativeStats(memberId);

        WeeklySummaryData lastWeekSummary = aiWeeklyReportRepository
                .findTopByMemberIdAndWeekStartDateBeforeOrderByWeekStartDateDesc(memberId, weekStartDate)
                .map(AiWeeklyReport::getWeeklySummaryData)
                .orElse(null);

        List<String> newTags = findNewTags(techData.getTags(), cumulativeData.getTagTotals());

        String ruleComment = RuleBasedCommentGenerator.generate(
                summaryData, techData, lastWeekSummary, newTags);

        WeeklyReportContext context = buildContext(member, summaryData, techData, cumulativeData, lastWeekSummary, newTags);

        // Gemini 호출 — 실패 시 CustomException 발생하여 저장되지 않음
        AiAnalysisResult aiResult = geminiClient.generateAiAnalysis(context);

        String aiAnalysisJson;
        try {
            aiAnalysisJson = objectMapper.writeValueAsString(aiResult);
        } catch (JsonProcessingException e) {
            log.error("AI 분석 결과 직렬화 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED);
        }

        AiWeeklyReport report = new AiWeeklyReport(
                member, weekStartDate, weekEndDate, summaryData, techData, cumulativeData, ruleComment);
        report.applyAiAnalysis(aiAnalysisJson);

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

    // ===== 누적 집계 =====

    private CumulativeStatsData buildCumulativeStats(Long memberId) {
        Object[] summaryRow = postRepository.findCumulativeSummary(memberId).get(0);
        int totalPosts   = ((Long) summaryRow[0]).intValue();
        int totalMinutes = ((Long) summaryRow[1]).intValue();

        Map<String, Integer> difficultyDist = new LinkedHashMap<>();
        postRepository.findCumulativeDifficultyDistribution(memberId).forEach(r -> {
            Difficulty d = (Difficulty) r[0];
            if (d != null) difficultyDist.put(d.name(), ((Long) r[1]).intValue());
        });

        Map<String, Integer> tagTotals      = new LinkedHashMap<>();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        int tagTotal = 0;
        for (Object[] r : postRepository.findCumulativeTagDistribution(memberId)) {
            String tagName = (String) r[0];
            int count = ((Long) r[1]).intValue();
            tagTotals.put(tagName, count);
            categoryCounts.merge(TechStackCategoryMapper.categorize(tagName).name(), count, Integer::sum);
            tagTotal += count;
        }

        Map<String, Integer> categoryPct = new LinkedHashMap<>();
        if (tagTotal > 0) {
            final int finalTotal = tagTotal;
            categoryCounts.forEach((cat, cnt) ->
                    categoryPct.put(cat, (int) Math.round((double) cnt / finalTotal * 100)));
        }

        return CumulativeStatsData.builder()
                .totalPosts(totalPosts)
                .totalLearningMinutes(totalMinutes)
                .categoryDistribution(categoryPct)
                .tagTotals(tagTotals)
                .difficultyDistribution(difficultyDist)
                .build();
    }

    // ===== Gemini 컨텍스트 빌드 =====

    private WeeklyReportContext buildContext(Member member,
                                              WeeklySummaryData summaryData,
                                              TechStackDistributionData techData,
                                              CumulativeStatsData cumulativeData,
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

        String statusLabel = member.getCurrentStatus() != null ? member.getCurrentStatus().getLabel() : null;
        String jobLabel    = member.getTargetJob()     != null ? member.getTargetJob().getLabel()     : null;

        return WeeklyReportContext.builder()
                .user(WeeklyReportContext.UserContext.builder()
                        .currentStatus(statusLabel)
                        .targetJob(jobLabel)
                        .build())
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
                .cumulativeStats(WeeklyReportContext.CumulativeStats.builder()
                        .totalPosts(cumulativeData.getTotalPosts())
                        .totalLearningMinutes(cumulativeData.getTotalLearningMinutes())
                        .categoryDistribution(cumulativeData.getCategoryDistribution())
                        .tagTotals(cumulativeData.getTagTotals())
                        .difficultyDistribution(cumulativeData.getDifficultyDistribution())
                        .build())
                .build();
    }

    // ===== 유틸 =====

    private List<String> findNewTags(Map<String, Integer> thisWeekTags, Map<String, Integer> cumulativeTags) {
        if (thisWeekTags == null) return Collections.emptyList();
        return thisWeekTags.entrySet().stream()
                .filter(e -> cumulativeTags.getOrDefault(e.getKey(), 0) - e.getValue() == 0)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    private AiWeeklyReportResponse toResponse(AiWeeklyReport report) {
        String aiAnalysisJson = report.getAiAnalysisComment();
        AiAnalysisResult parsedAiAnalysis = null;

        if (aiAnalysisJson != null && !aiAnalysisJson.isBlank()) {
            try {
                parsedAiAnalysis = objectMapper.readValue(aiAnalysisJson, AiAnalysisResult.class);
            } catch (JsonProcessingException e) {
                log.warn("AI 분석 파싱 실패 (reportId={}): {}", report.getReportId(), e.getMessage());
            }
        }

        String currentStatus = null;
        if (report.getMember() != null && report.getMember().getCurrentStatus() != null) {
            currentStatus = report.getMember().getCurrentStatus().name();
        }

        return AiWeeklyReportResponse.builder()
                .reportId(report.getReportId())
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .weeklySummary(report.getWeeklySummaryData())
                .techStackDistribution(report.getTechStackDistributionData())
                .cumulativeData(report.getCumulativeData())
                .ruleBasedComment(report.getRuleBasedComment())
                .aiAnalysisComment(aiAnalysisJson)
                .parsedAiAnalysis(parsedAiAnalysis)
                .currentStatus(currentStatus)
                .createdAt(report.getCreatedAt())
                .build();
    }
}