package com.tilog.domain.payback.service;

import com.tilog.domain.payback.dto.PaybackPolicyResponse;
import com.tilog.domain.payback.entity.PaybackPolicy;
import com.tilog.domain.payback.repository.PaybackPolicyRepository;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaybackPolicyService {
    private final PaybackPolicyRepository paybackPolicyRepository;

    public PaybackPolicyResponse getActivePolicy() {
        PaybackPolicy policy = getCurrentActivePolicy(LocalDate.now());

        return PaybackPolicyResponse.from(policy);
    }

    public PaybackPolicy getCurrentActivePolicy(LocalDate today) {
        return paybackPolicyRepository.findActivePolicies(today, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.ACTIVE_PAYBACK_POLICY_NOT_FOUND));
    }
}
