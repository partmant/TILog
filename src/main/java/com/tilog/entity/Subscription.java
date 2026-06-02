package com.tilog.entity;

import com.tilog.entity.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// 구독 엔티티
@Getter
@Entity
@Table(name = "subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Builder
    private Subscription(Member member, LocalDateTime endedAt, SubscriptionStatus status) {
        this.member = member;
        this.endedAt = endedAt;
        this.status = status;
    }

    // Mock 구독 생성 (30일)
    public static Subscription createMock(Member member) {
        return Subscription.builder()
                .member(member)
                .endedAt(LocalDateTime.now().plusDays(30))
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }

    // 구독 취소
    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
        this.endedAt = LocalDateTime.now();
    }

    // 구독 만료 처리
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    // 현재 구독이 유효한지 확인
    public boolean isActive() {
        if (this.status != SubscriptionStatus.ACTIVE) {
            return false;
        }
        if (this.endedAt == null) {
            return true;
        }
        return this.endedAt.isAfter(LocalDateTime.now());
    }
}
