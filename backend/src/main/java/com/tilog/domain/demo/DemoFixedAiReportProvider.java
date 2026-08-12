package com.tilog.domain.demo;

import com.tilog.domain.report.dto.AiAnalysisResult;

import java.util.List;

/**
 * 공개 데모 계정 전용 고정 AI 분석 응답.
 *
 * <p>데모 계정은 매일 자정 데이터가 초기화되기 때문에, 방문자가 리포트를 생성할 때마다
 * Gemini를 실제로 호출하면 반복 호출로 인한 API 비용이 발생한다. 데모 계정에 한해서는
 * 이 고정 응답을 사용해 실제 API 호출 없이 AI 주간 리포트 UI/UX를 동일하게 체험할 수 있게 한다.
 */
public final class DemoFixedAiReportProvider {

    private DemoFixedAiReportProvider() {
    }

    /** 데모 계정 게시글의 "핵심 요약" 기능(PostService#summarizePost)도 같은 이유로 고정 응답을 사용한다. */
    public static String fixedPostSummary() {
        return "이 요약은 공개 데모 계정용 고정 예시입니다. 실제 계정으로 TIL을 작성하면 " +
                "본문 내용을 바탕으로 한 실제 AI 요약을 확인할 수 있습니다.";
    }

    public static AiAnalysisResult fixedResult() {
        return AiAnalysisResult.builder()
                .weeklyPersona(AiAnalysisResult.WeeklyPersona.builder()
                        .title("백엔드 기초 다지기")
                        .totalEvaluation("이번 주는 백엔드 핵심 개념 위주로 학습 기록을 남겼습니다. " +
                                "특정 주제에 치우치지 않고 폭넓게 기록을 쌓아가는 초반 흐름이 확인됩니다. " +
                                "다음 주에는 심화 주제 하나를 정해 깊이를 더해보는 것을 추천합니다.")
                        .build())
                .deepTechAnalysis(AiAnalysisResult.DeepTechAnalysis.builder()
                        .focusArea("이번 주 기록은 서버 사이드 기본기(프레임워크, 데이터 접근 계층) 중심으로 " +
                                "구성되어 있습니다. 아직 특정 아키텍처 영역으로 확장되기 전 단계로, " +
                                "핵심 개념을 실습으로 확인하는 학습 패턴이 나타납니다.")
                        .intensityReview("난이도 분포상 기초~보통 수준의 학습이 주를 이룹니다. " +
                                "핵심 개념을 넓게 다지는 리프레시 주간으로 볼 수 있으며, " +
                                "다음 주에는 트러블슈팅형 심화 주제에 도전해볼 것을 권장합니다.")
                        .build())
                .careerAlignmentAudit(AiAnalysisResult.CareerAlignmentAudit.builder()
                        .status("INITIAL_STAGE")
                        .auditComment("이 리포트는 공개 데모 계정용 고정 예시 데이터입니다. " +
                                "실제 계정으로 TIL을 꾸준히 기록하면 목표 직무와 누적 학습 데이터를 " +
                                "비교한 개인 맞춤 분석을 받아볼 수 있습니다.")
                        .build())
                .practicalPortfolioAdvice(AiAnalysisResult.PracticalPortfolioAdvice.builder()
                        .resumeKeyword("백엔드 기초 역량, 데이터 접근 계층 이해")
                        .interviewQuestion("본인이 학습한 프레임워크에서 요청 하나가 처리되는 과정을 " +
                                "레이어별로 설명해볼 수 있나요?")
                        .build())
                .nextWeekRoadmap(AiAnalysisResult.NextWeekRoadmap.builder()
                        .actionItem("이번 주 학습한 주제 중 하나를 골라 트러블슈팅 관점에서 더 깊이 " +
                                "파고드는 TIL을 하나 이상 작성해보세요.")
                        .recommendedTechStacks(List.of(
                                AiAnalysisResult.RecommendedTechStack.builder()
                                        .techName("데이터베이스 인덱스")
                                        .reason("실무 성능 이슈의 상당수는 인덱스 설계에서 시작되므로 기초를 다져두면 좋습니다.")
                                        .build(),
                                AiAnalysisResult.RecommendedTechStack.builder()
                                        .techName("테스트 코드")
                                        .reason("작성한 기능을 검증하는 습관은 실무 코드 품질과 직결됩니다.")
                                        .build(),
                                AiAnalysisResult.RecommendedTechStack.builder()
                                        .techName("Git 브랜치 전략")
                                        .reason("협업 환경에서 자주 요구되는 실무 지식이라 미리 익혀두면 도움이 됩니다.")
                                        .build()
                        ))
                        .build())
                .mentorCheeringMessage("꾸준한 기록이 실력이 됩니다. 이번 주도 수고하셨어요!")
                .build();
    }
}
