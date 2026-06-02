package com.tilog.domain.payback.entity;

import com.tilog.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(
        name = "payback_participation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payback_participation_member_policy_month",
                        columnNames = {"member_id", "payback_policy_id", "participation_month"}
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
    @JoinColumn(name = "payback_policy_id", nullable = false)
    private PaybackPolicy paybackPolicy;

    @Column(name = "participation_month", nullable = false, length = 7)
    private String participationMonth;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mock_payment_status", nullable = false)
    private MockPaymentStatus mockPaymentStatus;

    @Column(name = "achieved_write_days", nullable = false)
    private int achievedWriteDays;

    @Column(name = "progress_rate", nullable = false)
    private int progressRate;

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
            PaybackPolicy paybackPolicy,
            String participationMonth
    ) {
        this.memberId = memberId;
        this.paybackPolicy = paybackPolicy;
        this.participationMonth = participationMonth;
        this.joinedAt = LocalDateTime.now();
        this.mockPaymentStatus = MockPaymentStatus.PAID;
        this.achievedWriteDays = 0;
        this.progressRate = 0;
        this.resultStatus = PaybackResultStatus.IN_PROGRESS;
        this.refundStatus = RefundStatus.NONE;
        this.refundAmount = paybackPolicy.getRefundAmount();
    }

    public static PaybackParticipation create(
            Long memberId,
            PaybackPolicy paybackPolicy,
            YearMonth participationMonth
    ) {
        return new PaybackParticipation(
                memberId,
                paybackPolicy,
                participationMonth.toString()
        );
    }

    public void updateProgress(int achievedWriteDays, int requiredWriteDays) {
        this.achievedWriteDays = achievedWriteDays;
        this.progressRate = calculateProgressRate(achievedWriteDays, requiredWriteDays);
    }

    private int calculateProgressRate(int achievedWriteDays, int requiredWriteDays) {
        if (requiredWriteDays <= 0) {
            return 0;
        }

        int rate = (int) Math.floor((double) achievedWriteDays / requiredWriteDays * 100);
        return Math.min(rate, 100);
    }
}
