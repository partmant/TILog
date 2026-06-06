package com.tilog.global.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilog.domain.report.dto.AiAnalysisResult;
import com.tilog.domain.report.dto.WeeklyReportContext;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestClient restClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    @PostConstruct
    private void init() {
        restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public AiAnalysisResult generateAiAnalysis(WeeklyReportContext context) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", buildPrompt(context))))),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "responseSchema", buildResponseSchema()
                    )
            );

            String responseBody = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/" + model + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String text = root
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            JsonNode aiReportNode = objectMapper.readTree(text).path("ai_weekly_report");
            return objectMapper.treeToValue(aiReportNode, AiAnalysisResult.class);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED);
        }
    }

    private String buildPrompt(WeeklyReportContext context) throws JsonProcessingException {
        WeeklyReportContext.UserContext user = context.getUser();
        boolean hasUserInfo = user != null
                && user.getCurrentStatus() != null
                && user.getTargetJob() != null;

        String userContextSection = hasUserInfo
                ? "- 현재 신분: " + user.getCurrentStatus() + "\n- 목표 직무: " + user.getTargetJob()
                : "유저의 현재 신분 및 목표 직무 정보가 입력되지 않았습니다.";

        String auditInstruction = hasUserInfo
                ? "현재 유저의 명확한 신분과 목표 직무를 누적 학습 데이터와 날카롭게 비교하여, " +
                  "현재 채용 시장 트렌드 기준으로 어떤 역량 밸런스가 깨져있는지 지적하고 이력서/면접 팁을 제공하세요."
                : "유저의 목표가 설정되지 않았으니 억지로 직무 멘토링을 하지 마십시오. " +
                  "가상의 목표를 상상(Hallucination)하는 것을 엄격히 금지합니다. " +
                  "오직 주어진 데이터 분포만 보고 '현재 어떤 파트 위주로 공부하고 있는지' 객관적 성향만 담백하게 분석해 " +
                  "[BIAS_WARNING] 대신 [INITIAL_STAGE] 상태로 넘기세요.";

        String thisWeekJson     = objectMapper.writeValueAsString(context.getThisWeek());
        String comparedJson     = objectMapper.writeValueAsString(context.getComparedToLastWeek());
        String cumulativeJson   = objectMapper.writeValueAsString(context.getCumulativeStats());

        return """
                [System Persona]
                당신은 IT 스타트업의 10년 차 테크 리드(Tech Lead)이자, 수많은 주니어 개발자를 채용하고 멘토링해 온 시니어 엔지니어입니다.
                제공된 유저의 정량적인 기술 학습 데이터를 바탕으로, 입에 발린 칭찬이 아닌 실무 관점에서의 날카롭고 깊이 있는 '주간 성장 인사이트 리포트'를 작성해야 합니다.

                [User Context]
                %s

                [Weekly Input Data]
                - 이번 주 데이터: %s
                - 지난주 대비 변동: %s
                - 누적 데이터: %s

                [Instructions & Guardrails]
                1. 현실적이고 전문적인 개발자 서적/테크 블로그 톤앤매너를 유지하세요. "열심히 하셨네요" 같은 모호한 문장은 금지합니다.
                2. [Focus & Intensity Review]: 학습 시간 대비 게시글 수, 'HARD' 난이도 비중을 체크하여 유저가 단순히 얕은 개념(Easy)만 많이 정리한 것인지, 하나의 개념을 깊게 파고든 것(Hard, 긴 몰입시간)인지 인과관계를 추론하여 짚어주세요.
                3. [Career Alignment Audit]: %s
                4. [Practical Portfolio Advice]: 유저의 현재 신분이 '취준생'인 경우 이력서에 즉시 녹일 수 있는 매력적인 키워드와 예상 면접 질문을, '재직자'인 경우 실무 아키텍처 설계나 리팩토링 관점의 키워드를 제안하세요. (정보가 없다면 기술 자체의 범용 기술 면접 질문 출제)
                5. [Next Week Roadmap]: 이번 주 공부한 기술 스택과 연계했을 때 가장 시너지가 나는 다음 단계의 기술 스택 3가지를 명확한 실무적 근거와 함께 추천하세요.
                6. [Weekly Persona Title 제약 조건]:
                   - '영토를 확장 중인 백엔드 전사', '지식의 마법사' 같은 유치하거나 판타지 게임 같은 오글거리는 비유적 표현은 엄격히 금지합니다.
                   - 현업 개발 용어(예: 딥다이브, 스케일아웃, 아키텍처, 파이프라인), 채용 시장 용어(예: T자형 인재, 올라운더, 스페셜리스트), 또는 담백한 데이터 요약 형태로 작성하세요.
                   - 글자 수는 15자 내외로 UI 대시보드 카드 타이틀에 딱 들어맞게 간결해야 합니다.

                [Output Format Constraint]
                반드시 지정된 JSON 스키마 구조에 맞춰 완벽한 순수 JSON 데이터만 반환하세요. 앞뒤 마크다운 기호(```json)나 인사말은 절대 포함하지 마십시오.
                """.formatted(userContextSection, thisWeekJson, comparedJson, cumulativeJson, auditInstruction);
    }

    private Map<String, Object> buildResponseSchema() {
        Map<String, Object> techStackItem = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "tech_name", Map.of("type", "STRING"),
                        "reason",    Map.of("type", "STRING")
                ),
                "required", List.of("tech_name", "reason")
        );

        Map<String, Object> weeklyPersona = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "title",            Map.of("type", "STRING"),
                        "total_evaluation", Map.of("type", "STRING")
                ),
                "required", List.of("title", "total_evaluation")
        );

        Map<String, Object> deepTechAnalysis = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "focus_area",       Map.of("type", "STRING"),
                        "intensity_review", Map.of("type", "STRING")
                ),
                "required", List.of("focus_area", "intensity_review")
        );

        Map<String, Object> careerAlignmentAudit = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "status",        Map.of("type", "STRING"),
                        "audit_comment", Map.of("type", "STRING")
                ),
                "required", List.of("status", "audit_comment")
        );

        Map<String, Object> practicalPortfolioAdvice = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "resume_keyword",    Map.of("type", "STRING"),
                        "interview_question", Map.of("type", "STRING")
                ),
                "required", List.of("resume_keyword", "interview_question")
        );

        Map<String, Object> nextWeekRoadmap = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "action_item", Map.of("type", "STRING"),
                        "recommended_tech_stacks", Map.of(
                                "type", "ARRAY",
                                "items", techStackItem
                        )
                ),
                "required", List.of("action_item", "recommended_tech_stacks")
        );

        Map<String, Object> aiWeeklyReportProps = Map.of(
                "weekly_persona",            weeklyPersona,
                "deep_tech_analysis",        deepTechAnalysis,
                "career_alignment_audit",    careerAlignmentAudit,
                "practical_portfolio_advice", practicalPortfolioAdvice,
                "next_week_roadmap",         nextWeekRoadmap,
                "mentor_cheering_message",   Map.of("type", "STRING")
        );

        Map<String, Object> aiWeeklyReport = Map.of(
                "type", "OBJECT",
                "properties", aiWeeklyReportProps,
                "required", List.of(
                        "weekly_persona", "deep_tech_analysis", "career_alignment_audit",
                        "practical_portfolio_advice", "next_week_roadmap", "mentor_cheering_message"
                )
        );

        return Map.of(
                "type", "OBJECT",
                "properties", Map.of("ai_weekly_report", aiWeeklyReport),
                "required", List.of("ai_weekly_report")
        );
    }
}