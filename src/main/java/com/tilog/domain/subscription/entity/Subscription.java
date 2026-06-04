package com.tilog.domain.subscription.entity;

import com.tilog.domain.member.entity.Member;
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

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Builder
    private Subscription(
            Member member,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            SubscriptionStatus status
    ) {
        this.member = member;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
    }

    // Mock 구독 생성
    public static Subscription createMock(Member member) {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime endedAt = startedAt.plusMonths(1).minusSeconds(1);

        return Subscription.builder()
                .member(member)
                .startedAt(startedAt)
                .endedAt(endedAt)
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
