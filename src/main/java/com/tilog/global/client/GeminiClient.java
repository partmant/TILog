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
}