package com.tilog.domain.payback.controller;

import com.tilog.domain.payback.dto.PaybackParticipationRequest;
import com.tilog.domain.payback.dto.PaybackParticipationResponse;
import com.tilog.domain.payback.service.PaybackParticipationService;
import com.tilog.global.response.ApiResponse;
import com.tilog.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payback-participations")
@RequiredArgsConstructor
public class PaybackParticipationController {

    private final PaybackParticipationService paybackParticipationService;

    @PostMapping
    public ApiResponse<PaybackParticipationResponse> participate(
            @Valid @RequestBody PaybackParticipationRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        PaybackParticipationResponse response = paybackParticipationService.participate(memberId, request);
        return ApiResponse.success(response, "페이백 챌린지 참여 성공");
    }

    @GetMapping("/me")
    public ApiResponse<PaybackParticipationResponse> getMyParticipation(
            @RequestParam String month
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        PaybackParticipationResponse response = paybackParticipationService.getMyParticipation(memberId, month);
        return ApiResponse.success(response);
    }
}
