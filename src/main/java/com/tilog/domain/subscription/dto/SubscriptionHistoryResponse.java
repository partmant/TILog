package com.tilog.domain.subscription.dto;

import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.domain.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;

//  구독 이력
public record SubscriptionHistoryResponse(
        Long subscriptionId,
        SubscriptionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static SubscriptionHistoryResponse from(Subscription subscription) {
        return new SubscriptionHistoryResponse(
                subscription.getId(),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getEndedAt()
        );
    }
}
