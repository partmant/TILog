package com.tilog.domain.subscription.controller;

import com.tilog.domain.subscription.dto.SubscriptionHistoryResponse;
import com.tilog.domain.subscription.dto.SubscriptionStatusResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mock 구독 API
 *
 * POST   /api/subscriptions          - Mock 구독 신청 (30일 PREMIUM 권한 부여)
 * DELETE /api/subscriptions          - 구독 취소 (PREMIUM → USER 권한 복원)
 * GET    /api/subscriptions/me       - 내 구독 상태 조회
 * GET    /api/subscriptions/me/history - 내 구독 이력 조회
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

  // mock 구독 신청
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> subscribe() {
        SubscriptionStatusResponse response = subscriptionService.subscribe();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "프리미엄 구독이 시작되었습니다."));
    }

   // 구독 취소
    @DeleteMapping
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> cancel() {
        SubscriptionStatusResponse response = subscriptionService.cancel();
        return ResponseEntity.ok(ApiResponse.success(response, "구독이 취소되었습니다."));
    }

    // 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getMyStatus() {
        SubscriptionStatusResponse response = subscriptionService.getMyStatus();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 구독 이력 조회
     */
    @GetMapping("/me/history")
    public ResponseEntity<ApiResponse<List<SubscriptionHistoryResponse>>> getMyHistory() {
        List<SubscriptionHistoryResponse> history = subscriptionService.getMyHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
