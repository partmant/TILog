package com.tilog.domain.payback.controller;

import com.tilog.domain.payback.dto.PaybackParticipationResponse;
import com.tilog.domain.payback.service.PaybackParticipationService;
import com.tilog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payback-participations")
@RequiredArgsConstructor
public class PaybackParticipationController {
    private final PaybackParticipationService paybackParticipationService;

    @GetMapping("/me/current")
    public ApiResponse<PaybackParticipationResponse> getCurrentParticipation() {
        PaybackParticipationResponse response = paybackParticipationService.getCurrentParticipation();

        return ApiResponse.success(response);
    }
}
