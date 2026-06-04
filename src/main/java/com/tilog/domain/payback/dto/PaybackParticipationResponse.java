package com.tilog.domain.payback.dto;

import com.tilog.domain.payback.entity.MockPaymentStatus;
import com.tilog.domain.payback.entity.PaybackParticipation;
import com.tilog.domain.payback.entity.PaybackResultStatus;
import com.tilog.domain.payback.entity.RefundStatus;

public record PaybackParticipationResponse(
        Long paybackParticipationId,
        Long memberId,
        Long paybackPolicyId,
        String policyName,
        String participationMonth,
        MockPaymentStatus mockPaymentStatus,
        int requiredWriteDays,
        int achievedWriteDays,
        int progressRate,
        PaybackResultStatus resultStatus,
        RefundStatus refundStatus,
        int refundAmount
) {
    public static PaybackParticipationResponse from(PaybackParticipation participation) {
        return new PaybackParticipationResponse(
                participation.getPaybackParticipationId(),
                participation.getMemberId(),
                participation.getPaybackPolicy().getPaybackPolicyId(),
                participation.getPaybackPolicy().getName(),
                participation.getParticipationMonth(),
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
        int progressRate = calculateProgressRate(achievedWriteDays, requiredWriteDays);

        return new PaybackParticipationResponse(
                participation.getPaybackParticipationId(),
                participation.getMemberId(),
                participation.getPaybackPolicy().getPaybackPolicyId(),
                participation.getPaybackPolicy().getName(),
                participation.getParticipationMonth(),
                participation.getMockPaymentStatus(),
                requiredWriteDays,
                achievedWriteDays,
                progressRate,
                participation.getResultStatus(),
                participation.getRefundStatus(),
                participation.getRefundAmount()
        );
    }

    private static int calculateProgressRate(int achievedWriteDays, int requiredWriteDays) {
        if (requiredWriteDays <= 0) {
            return 0;
        }

        int rate = (int) Math.floor((double) achievedWriteDays / requiredWriteDays * 100);
        return Math.min(rate, 100);
    }
}
