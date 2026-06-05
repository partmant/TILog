package com.tilog.domain.subscription.scheduler;

import com.tilog.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 구독 만료 스케줄러
 *
 * 매 정각 실행:
 * - CANCEL_RESERVED 만료 → EXPIRED + 멤버 역할 USER 복원
 * - ACTIVE 만료 → EXPIRED 후 신규 구독 자동 갱신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 * * * *") // 매 정각 실행
    public void processExpiredSubscriptions() {
        log.info("[스케줄러] 만료 구독 처리 시작");
        try {
            subscriptionService.processExpiredSubscriptions();
            log.info("[스케줄러] 만료 구독 처리 완료");
        } catch (Exception e) {
            log.error("[스케줄러] 만료 구독 처리 중 오류 발생", e);
        }
    }
}
