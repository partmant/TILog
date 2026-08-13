package com.tilog.domain.demo.scheduler;

import com.tilog.domain.demo.service.DemoDataResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "demo.enabled", havingValue = "true")
public class DemoDataResetScheduler {

    private final DemoDataResetService demoDataResetService;

    // 매일 자정 실행 — 공개 데모 계정(demo.account.email, demo.mentor.email)이 남긴
    // TIL·스트릭·구독 데이터를 초기화한다. 한쪽이 실패해도 나머지는 이어서 처리한다.
    @Scheduled(cron = "${demo.reset.cron:0 0 0 * * *}")
    public void resetDemoData() {
        log.info("데모 데이터 초기화 스케줄러 시작");
        try {
            demoDataResetService.resetDemoData();
        } catch (Exception e) {
            log.error("데모 계정 초기화 중 오류 발생", e);
        }

        try {
            demoDataResetService.resetDemoMentorData();
        } catch (Exception e) {
            log.error("데모 멘토 계정 초기화 중 오류 발생", e);
        }
    }
}
