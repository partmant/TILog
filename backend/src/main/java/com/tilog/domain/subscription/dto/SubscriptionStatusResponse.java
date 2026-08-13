package com.tilog.domain.subscription.dto;

import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.domain.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;

// 수독 상태
public record SubscriptionStatusResponse(
        Long subscriptionId,
        String memberNickname,
        SubscriptionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        boolean isActive,
        // 구독으로 회원 권한(role)이 바뀐 경우에만 채워진다.
        // 기존 accessToken에는 예전 role이 그대로 박혀있어, 클라이언트가 이 값으로
        // 즉시 토큰을 교체하지 않으면 재로그인 전까지 프리미엄 전용 기능에 접근할 수 없다.
        String accessToken
) {
    public static SubscriptionStatusResponse from(Subscription subscription) {
        return from(subscription, null);
    }

    public static SubscriptionStatusResponse from(Subscription subscription, String accessToken) {
        return new SubscriptionStatusResponse(
                subscription.getId(),
                subscription.getMember().getNickname(),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getEndedAt(),
                subscription.isActive(),
                accessToken
        );
    }

    // 구독 없음 상태
    public static SubscriptionStatusResponse noSubscription(String nickname) {
        return new SubscriptionStatusResponse(
                null,
                nickname,
                null,
                null,
                null,
                false,
                null
        );
    }
}
