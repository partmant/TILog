package com.tilog.dto.subscription;

import com.tilog.entity.Subscription;
import com.tilog.entity.enums.SubscriptionStatus;

import java.time.LocalDateTime;

// 수독 상태
public record SubscriptionStatusResponse(
        Long subscriptionId,
        String memberNickname,
        SubscriptionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        boolean isActive
) {
    public static SubscriptionStatusResponse from(Subscription subscription) {
        return new SubscriptionStatusResponse(
                subscription.getId(),
                subscription.getMember().getNickname(),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getEndedAt(),
                subscription.isActive()
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
                false
        );
    }
}
