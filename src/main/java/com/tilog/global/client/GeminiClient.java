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
import java.util.StringJoiner;

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

    // 게시글 상세의 핵심 요약 생성
    public String generatePostSummary(String title, String content, List<String> tagNames) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", buildPostSummaryPrompt(title, content, tagNames))))),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "responseSchema", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of("summary", Map.of("type", "STRING")),
                                    "required", List.of("summary")
                            )
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

            return objectMapper.readTree(text).path("summary").asText();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini post summary failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED);
        }
    }

    // 주간레포트 프롬프트 빌드
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

        String portfolioInstruction = buildPortfolioAdviceInstruction(user != null ? user.getCurrentStatus() : null);
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
                2. [Focus & Intensity Review]: ① [focus_area] (이번 주 집중한 기술 아키텍처 분석):
                                                - 이번 주 유저가 사용한 상세 기술 스택 분포(tag_distribution)와 카테고리를 연계하여, 실무 아키텍처 관점에서 어떤 영역에 집중했는지 정밀 분석하세요.
                                                - 단순히 "Spring을 공부했습니다"가 아니라, 유저가 선택한 목표 직무(예: FULLSTACK)와 연결하여 "백엔드 프레임워크(Spring)와 데이터 접근 계층(JPA)의 인터랙션에 집중하며 서버 사이드 아키텍처의 기반을 다진 주간"과 같이 실무적이고 입체적인 기술 맥락으로 재해석하여 작성하세요.
                                              ② [intensity_review] (몰입 시간과 난이도 조합을 통한 학습 깊이 진단):
                                              - 이번 주 난이도 분포 데이터(EASY, NORMAL, HARD)를 기반으로 유저의 이번 주 학습 성향을 '객관적이고 담백하게' 진단하세요.(채찍질이나 영혼 없는 과도한 칭찬은 금지합니다.)
                                              - [EASY/NORMAL 비중이 높을 때]:
                                                현재 단계는 핵심 개념들의 기초 뼈대를 탄탄하게 다지고 넓게 확장하는 '리프레시 및 스펙트럼 확장 주간'임을 짚어주세요.
                                                다만, 여기에 머무르지 않고 실무 역량을 한 단계 더 끌어올리기 위해서는 다음 주에 중요도가 높은 핵심 주제 하나를 정해 'HARD(심화 트러블슈팅, 아키텍처 깊이 파고들기)' 난이도에 도전해 보는 것을 권장한다고 자연스럽게 빌드업하세요.
                                              - [HARD 비중이 높을 때]:
                                                문제를 깊이 있게 파고들어 끝까지 해결해 내는 '딥다이브(Deep-Dive) 주간'이었음을 인정해 주고 담백하게 칭찬해주세요.
                                              - 요약하자면, 현재 학습 스펙트럼의 장점을 데이터 기반으로 짚어준 뒤, 다음 단계로 나아가기 위한 '난이도 밸런스 가이드'를 제안하는 톤을 유지하세요. 채찍질이나 과도한 칭찬 모두 금지합니다.
                3. [Career Alignment Audit]: %s
                4. [Practical Portfolio Advice]: %s
                5. [Next Week Roadmap]: 이번 주 공부한 기술 스택과 연계했을 때 가장 시너지가 나는 다음 단계의 기술 스택 3가지를 명확한 실무적 근거와 함께 추천하세요.
                6. [Weekly Persona Title 제약 조건]:
                   - '영토를 확장 중인 백엔드 전사', '지식의 마법사' 같은 유치하거나 판타지 게임 같은 오글거리는 비유적 표현은 엄격히 금지합니다.
                   - 현업 개발 용어(예: 딥다이브, 스케일아웃, 아키텍처, 파이프라인), 채용 시장 용어(예: T자형 인재, 올라운더, 스페셜리스트), 또는 담백한 데이터 요약 형태로 작성하세요.
                   - 글자 수는 15자 내외로 UI 대시보드 카드 타이틀에 딱 들어맞게 간결해야 합니다.
                7. [OTHER 카테고리 분석 규칙 - 필수 필독]:
                 - 기술 카테고리 분포(category_distribution) 데이터 중 'OTHER' 항목이 높게 나타날 수 있습니다. 이는 시스템 분류상 백엔드/프론트엔드에 속하지 않는 [CS 지식(자료구조, 알고리즘, 네트워크), 보안, 데이터베이스 인프라, 형상관리(Git)] 등이 포함되어 있기 때문입니다.
                 - 절대로 'OTHER'라는 단어만 보고 "기타 영역에 치우쳐 있다"거나 "중요하지 않은 학습을 하고 있다"고 평가하지 마십시오.
                 - 'OTHER' 비율이 높다면, 반드시 세부 기술 스택 분포(tag_distribution 또는 tag_totals)를 역추적하여 어떤 기술 태그가 들어있는지 확인하세요.
                   - 만약 자료구조, 알고리즘, 네트워크 등이 있다면 -> "탄탄한 컴퓨터 공학(CS) 엔지니어링 기본기를 다지는 고부가가치 학습을 했다"고 분석하세요.
                   - 만약 Spring Security, OAuth, JWT 등이 들어있다면 -> "애플리케이션의 안정성을 높이는 보안 아키텍처 수립에 집중했다"고 분석하세요.
                 - 즉, 겉모습인 카테고리명(OTHER)에 갇히지 말고, 실제 알맹이인 '기술 스택 태그'를 기준으로 학습의 진짜 가치를 판단하여 [focus_area]와 [intensity_review]를 작성해야 합니다.
                 8. [JSON 항목별 분량 및 글자 수 제한 규칙 - UI 최적화]:
                   - 대시보드 화면의 카드 레이아웃 균형을 위해, AI는 반드시 다음 지정된 글자 수(공백 포함, 한글 기준) 범위를 엄격히 준수하여 응답해야 합니다.\s
                   - 문장은 중간에 끊기지 않고 완결된 문장형태로 마쳐야 합니다.
                   [weekly_persona]
                  - title: 10자 ~ 15자 내외 (UI 타이틀 텍스트 한 줄 제한)
                  - total_evaluation: 150자 ~ 200자 내외 (약 3~4문장)
            
                  [deep_tech_analysis]
                  - focus_area: 150자 ~ 200자 내외 (기술 아키텍처 맥락 서술, 약 3~4문장)
                  - intensity_review: 150자 ~ 200자 내외 (난이도 및 학습 깊이 진단, 약 3~4문장)
            
                  [career_alignment_audit]
                  - status: BALANCED / BIAS_WARNING / INITIAL_STAGE 중 딱 하나만 출력 (영어 대문자 고정)
                  - audit_comment: 150자 ~ 200자 내외 (역량 밸런스 피드백, 약 3~4문장)
            
                  [practical_portfolio_advice]
                  - resume_keyword: 30자 ~ 50자 내외 (이력서용  핵심 기술 키워드)
                  - interview_question: 60자 ~ 100자 내외 (구체적이고 날카로운 예상 질문 1개)
            
                  [next_week_roadmap]
                  - action_item: 100자 ~ 130자 내외 (다음 주 실천 과제 가이드, 약 2문장)
                  - recommended_tech_stacks (리스트 내부 각 항목):
                    * tech_name: 10자 내외 (기술 스택명 혹은 개념명 단어)
                    * reason: 60자 ~ 80자 내외 (해당 기술을 추천하는 실무적 근거 1줄)
            
                  [mentor_cheering_message]
                  - mentor_cheering_message: 50자 ~ 80자 내외 (간결하고 따뜻한 격려 문구 1~2문장)

                [Output Format Constraint]
                반드시 지정된 JSON 스키마 구조에 맞춰 완벽한 순수 JSON 데이터만 반환하세요. 앞뒤 마크다운 기호(```json)나 인사말은 절대 포함하지 마십시오.
                """.formatted(userContextSection, thisWeekJson, comparedJson, cumulativeJson, auditInstruction, portfolioInstruction);
    }

    private String buildPortfolioAdviceInstruction(String currentStatusLabel) {
        if (currentStatusLabel == null) {
            return "클라이언트 미팅이나 제안서에서 본인의 기술적 전문성을 어필할 수 있는 기술적 셀링 포인트 문구를 resume_keyword로, " +
                   "실제 외주/개발 프로젝트 요구사항 분석 시 반드시 검토해야 할 기술적 제약사항 및 예외 처리 질문을 interview_question으로 작성하세요.";
        }
        return switch (currentStatusLabel) {
            case "취준생", "이직준비자" ->
                    "이력서/포트폴리오에 즉시 녹일 수 있는 매력적인 핵심 기술 역량 문구를 resume_keyword로, " +
                    "실제 채용 과정에서 마주할 법한 날카로운 기술 면접 예상 질문을 interview_question으로 작성하세요.";
            case "재직자" ->
                    "실무 프로덕션 레벨에서 리팩토링, 아키텍처 개선, 성능 최적화 관점으로 접근할 수 있는 기술 키워드를 resume_keyword로, " +
                    "팀 내 코드 리뷰나 사내 기술 세미나에서 깊이 있게 논의하기 좋은 아키텍처 토론 주제를 interview_question으로 작성하세요.";
            case "학생" ->
                    "전공 학업, 대외 활동, 토이 프로젝트에 활용하기 좋은 CS 및 기반 기술 개념 키워드를 resume_keyword로, " +
                    "개발 동아리 면접이나 전공 지식 검증을 위한 핵심 이론 질문을 interview_question으로 작성하세요.";
            default ->
                    "클라이언트 미팅이나 제안서에서 본인의 기술적 전문성을 어필할 수 있는 기술적 셀링 포인트 문구를 resume_keyword로, " +
                    "실제 외주/개발 프로젝트 요구사항 분석 시 반드시 검토해야 할 기술적 제약사항 및 예외 처리 질문을 interview_question으로 작성하세요.";
        };
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

    // 게시글 제목/본문/태그를 요약 프롬프트로 변환
    private String buildPostSummaryPrompt(String title, String content, List<String> tagNames) {
        StringJoiner tagJoiner = new StringJoiner(", ");
        if (tagNames != null) {
            tagNames.stream()
                    .filter(tagName -> tagName != null && !tagName.isBlank())
                    .forEach(tagJoiner::add);
        }

        return """
                당신은 개발 학습 기록을 복습하기 쉽게 요약하는 튜터입니다.
                아래 TIL 게시글을 읽고 핵심 개념만 한국어로 요약하세요.

                제목:
                %s

                태그:
                %s

                본문:
                %s

                요구사항:
                - 게시글에 실제로 있는 내용만 사용
                - 핵심 개념 2~4개를 자연스러운 문장으로 정리
                - 불필요한 칭찬, 추측, 새 학습 주제 제안은 제외
                - 250자 이내
                - JSON 형식으로만 응답
                """.formatted(
                safeText(title),
                tagJoiner.length() == 0 ? "없음" : tagJoiner.toString(),
                safeText(content)
        );
    }

    private String safeText(String text) {
        return text == null || text.isBlank() ? "없음" : text;
    }
}
