package com.tilog.global.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${gemini.api.model:gemini-2.0-flash}")
    private String model;

    @PostConstruct
    private void init() {
        restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String generateAiAnalysis(WeeklyReportContext context) {
        try {
            String contextJson = objectMapper.writeValueAsString(context);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", buildPrompt(contextJson))))),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "responseSchema", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of("analysis", Map.of("type", "STRING")),
                                    "required", List.of("analysis")
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

            return objectMapper.readTree(text).path("analysis").asText();

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

    private String buildPrompt(String contextJson) {
        return """
                당신은 개발자 성장 코치입니다. 아래 개발자의 주간 TIL(Today I Learned) 작성 데이터를 분석하여
                구체적이고 격려적인 성장 분석을 한국어로 작성해주세요.

                분석 데이터:
                %s

                요구사항:
                - 이번 주 학습 패턴의 강점을 구체적으로 언급
                - 기술 스택 다양성과 깊이에 대한 인사이트 제공
                - 지난주 데이터가 있을 경우에만, 지난주 대비 성장 포인트 강조 (지난주 데이터가 없으면 지난주 언급 절대 X)
                - 다음 주에 집중하면 좋을 영역 제안
                - 300자 이내의 자연스럽고 친근한 한국어로 작성

                JSON 형식으로 응답하세요.
                """.formatted(contextJson);
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
