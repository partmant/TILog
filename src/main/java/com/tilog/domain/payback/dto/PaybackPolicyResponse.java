package com.tilog.domain.payback.dto;

import com.tilog.domain.payback.entity.PaybackPolicy;

import java.time.LocalDate;

public record PaybackPolicyResponse(
        Long paybackPolicyId,
        String name,
        int requiredWriteDays,
        int subscriptionFee,
        int refundAmount,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
    public static PaybackPolicyResponse from(PaybackPolicy policy) {
        return new PaybackPolicyResponse(
                policy.getPaybackPolicyId(),
                policy.getName(),
                policy.getRequiredWriteDays(),
                policy.getSubscriptionFee(),
                policy.getRefundAmount(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.isActive()
        );
    }
}
