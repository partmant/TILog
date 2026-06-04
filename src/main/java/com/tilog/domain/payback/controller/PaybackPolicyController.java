package com.tilog.domain.payback.controller;

import com.tilog.domain.payback.dto.PaybackPolicyResponse;
import com.tilog.domain.payback.service.PaybackPolicyService;
import com.tilog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payback-policies")
@RequiredArgsConstructor
public class PaybackPolicyController {
    private final PaybackPolicyService paybackPolicyService;

    @GetMapping("/active")
    public ApiResponse<PaybackPolicyResponse> getActivePolicy() {
        PaybackPolicyResponse response = paybackPolicyService.getActivePolicy();
        return ApiResponse.success(response);
    }
}
