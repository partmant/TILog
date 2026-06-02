package com.tilog.controller;

import com.tilog.dto.streak.StreakStatResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.service.streak.StreakStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/write-histories")
@RequiredArgsConstructor
public class StreakStatController {
    private final StreakStatService streakStatService;

    @GetMapping("/streak")
    public ApiResponse<StreakStatResponse> getMyStreak(
            @RequestHeader("X-MEMBER-ID") Long memberId
    ) {
        StreakStatResponse response = streakStatService.getStreak(memberId);
        return ApiResponse.success(response);
    }
}
