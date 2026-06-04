package com.tilog.domain.payback.entity;

import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payback_participation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payback_participation_subscription",
                        columnNames = {"subscription_id"}
                ),
                @UniqueConstraint(
                        name = "uk_payback_participation_member_period",
                        columnNames = {"member_id", "period_start_date", "period_end_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaybackParticipation extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payback_participation_id")
    private Long paybackParticipationId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payback_policy_id", nullable = false)
    private PaybackPolicy paybackPolicy;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mock_payment_status", nullable = false)
    private MockPaymentStatus mockPaymentStatus;

    @Column(name = "achieved_write_days", nullable = false)
    private int achievedWriteDays;

    @Column(name = "progress_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    private PaybackResultStatus resultStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus;

    @Column(name = "refund_amount", nullable = false)
    private int refundAmount;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    private PaybackParticipation(
            Long memberId,
            Subscription subscription,
            PaybackPolicy paybackPolicy,
            LocalDate periodStartDate,
            LocalDate periodEndDate
    ) {
        this.memberId = memberId;
        this.subscription = subscription;
        this.paybackPolicy = paybackPolicy;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.joinedAt = LocalDateTime.now();
        this.mockPaymentStatus = MockPaymentStatus.PAID;
        this.achievedWriteDays = 0;
        this.progressRate = BigDecimal.ZERO;
        this.resultStatus = PaybackResultStatus.IN_PROGRESS;
        this.refundStatus = RefundStatus.NONE;
        this.refundAmount = paybackPolicy.getRefundAmount();
    }

    public static PaybackParticipation create(
            Long memberId,
            Subscription subscription,
            PaybackPolicy paybackPolicy
    ) {
        return new PaybackParticipation(
                memberId,
                subscription,
                paybackPolicy,
                subscription.getStartedAt().toLocalDate(),
                subscription.getEndedAt().toLocalDate()
        );
    }

    public void cancel() {
        this.resultStatus = PaybackResultStatus.FAILED;
        this.settledAt = LocalDateTime.now();
    }

    // 구독 연장 시 페이백 종료 일자 연장
    public void extendPeriod(LocalDate newEndDate) {
        this.periodEndDate = newEndDate;
    }

    // 같은 기간에 재구독할 때 기존 레코드를 재사용
    public void reactivate(Subscription newSubscription) {
        this.subscription = newSubscription;
        this.achievedWriteDays = 0;
        this.progressRate = BigDecimal.ZERO;
        this.resultStatus = PaybackResultStatus.IN_PROGRESS;
        this.refundStatus = RefundStatus.NONE;
        this.joinedAt = LocalDateTime.now();
        this.settledAt = null;
    }

    public void updateProgress(int achievedWriteDays, int requiredWriteDays) {
        this.achievedWriteDays = achievedWriteDays;
        this.progressRate = calculateProgressRate(achievedWriteDays, requiredWriteDays);
    }

    private BigDecimal calculateProgressRate(int achievedWriteDays, int requiredWriteDays) {
        if (requiredWriteDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = BigDecimal.valueOf(achievedWriteDays)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(requiredWriteDays), 2, RoundingMode.DOWN);

        return rate.min(BigDecimal.valueOf(100));
    }
}
