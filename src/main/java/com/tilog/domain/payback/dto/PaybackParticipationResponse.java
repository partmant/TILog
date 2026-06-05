package com.tilog.domain.payback.dto;

import com.tilog.domain.payback.entity.MockPaymentStatus;
import com.tilog.domain.payback.entity.PaybackParticipation;
import com.tilog.domain.payback.entity.PaybackResultStatus;
import com.tilog.domain.payback.entity.RefundStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record PaybackParticipationResponse(
        Long paybackParticipationId,
        Long memberId,
        Long subscriptionId,
        Long paybackPolicyId,
        String policyName,
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        MockPaymentStatus mockPaymentStatus,
        int requiredWriteDays,
        int achievedWriteDays,
        BigDecimal progressRate,
        PaybackResultStatus resultStatus,
        RefundStatus refundStatus,
        int refundAmount
) {
    public static PaybackParticipationResponse from(PaybackParticipation participation) {
        return new PaybackParticipationResponse(
                participation.getPaybackParticipationId(),
                participation.getMemberId(),
                participation.getSubscription().getId(),
                participation.getPaybackPolicy().getPaybackPolicyId(),
                participation.getPaybackPolicy().getName(),
                participation.getPeriodStartDate(),
                participation.getPeriodEndDate(),
                participation.getMockPaymentStatus(),
                participation.getPaybackPolicy().getRequiredWriteDays(),
                participation.getAchievedWriteDays(),
                participation.getProgressRate(),
                participation.getResultStatus(),
                participation.getRefundStatus(),
                participation.getRefundAmount()
        );
    }

    public static PaybackParticipationResponse from(
            PaybackParticipation participation,
            int achievedWriteDays
    ) {
        int requiredWriteDays = participation.getPaybackPolicy().getRequiredWriteDays();
        BigDecimal progressRate = calculateProgressRate(achievedWriteDays, requiredWriteDays);

        return new PaybackParticipationResponse(
                participation.getPaybackParticipationId(),
                participation.getMemberId(),
                participation.getSubscription().getId(),
                participation.getPaybackPolicy().getPaybackPolicyId(),
                participation.getPaybackPolicy().getName(),
                participation.getPeriodStartDate(),
                participation.getPeriodEndDate(),
                participation.getMockPaymentStatus(),
                requiredWriteDays,
                achievedWriteDays,
                progressRate,
                participation.getResultStatus(),
                participation.getRefundStatus(),
                participation.getRefundAmount()
        );
    }

    private static BigDecimal calculateProgressRate(int achievedWriteDays, int requiredWriteDays) {
        if (requiredWriteDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = BigDecimal.valueOf(achievedWriteDays)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(requiredWriteDays), 2, RoundingMode.DOWN);

        return rate.min(BigDecimal.valueOf(100));
    }
}
