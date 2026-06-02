package com.tilog.domain.payback.dto;

import com.tilog.domain.payback.entity.PaybackParticipation;

public record PaybackParticipationResponse(
        Long paybackParticipationId,
        Long memberId,
        Long paybackPolicyId,
        String policyName,
        String participationMonth,
        String mockPaymentStatus,
        int requiredWriteDays,
        int achievedWriteDays,
        int progressRate,
        String resultStatus,
        String refundStatus,
        int refundAmount
) {
    public static PaybackParticipationResponse from(PaybackParticipation participation) {
        return new PaybackParticipationResponse(
                participation.getPaybackParticipationId(),
                participation.getMemberId(),
                participation.getPaybackPolicy().getPaybackPolicyId(),
                participation.getPaybackPolicy().getName(),
                participation.getParticipationMonth(),
                participation.getMockPaymentStatus().name(),
                participation.getPaybackPolicy().getRequiredWriteDays(),
                participation.getAchievedWriteDays(),
                participation.getProgressRate(),
                participation.getResultStatus().name(),
                participation.getRefundStatus().name(),
                participation.getRefundAmount()
        );
    }
}
