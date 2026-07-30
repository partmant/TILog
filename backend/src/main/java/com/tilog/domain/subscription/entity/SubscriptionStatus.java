package com.tilog.domain.subscription.entity;

public enum SubscriptionStatus {
    ACTIVE,
    CANCEL_RESERVED, // 취소 예약됨 (기간 만료 전까지 혜택 유지)
    EXPIRED,
    CANCELED
}
