package com.tilog.domain.payback.service;

import com.tilog.domain.payback.dto.PaybackPolicyResponse;
import com.tilog.domain.payback.entity.PaybackPolicy;
import com.tilog.domain.payback.repository.PaybackPolicyRepository;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaybackPolicyService {
    private final PaybackPolicyRepository paybackPolicyRepository;

    public PaybackPolicyResponse getActivePolicy() {
        LocalDate today = LocalDate.now();

        PaybackPolicy policy = paybackPolicyRepository
                .findFirstByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(today, today)
                .orElseThrow(() -> new CustomException(ErrorCode.ACTIVE_PAYBACK_POLICY_NOT_FOUND));

        return PaybackPolicyResponse.from(policy);
    }
}
