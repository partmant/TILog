package com.tilog.domain.writeHistory.controller;

import com.tilog.domain.writeHistory.dto.WriteHistoryDailyCountSummaryResponse;
import com.tilog.domain.writeHistory.dto.WriteHistoryHeatmapResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.writeHistory.service.WriteHistoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/write-histories")
@RequiredArgsConstructor
public class WriteHistoryQueryController {
    private final WriteHistoryQueryService writeHistoryQueryService;

    @GetMapping("/daily-counts")
    public ApiResponse<WriteHistoryDailyCountSummaryResponse> getDailyCounts(
            // TODO: 인증 구현 후 X-MEMBER-ID 헤더 제거하고, SecurityContext 또는 @AuthenticationPrincipal에서 memberId를 가져오도록 수정
            @RequestHeader("X-MEMBER-ID") Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        WriteHistoryDailyCountSummaryResponse response =
                writeHistoryQueryService.getDailyCounts(memberId, startDate, endDate);

        return ApiResponse.success(response);
    }

    @GetMapping("/heatmap")
    public ApiResponse<WriteHistoryHeatmapResponse> getHeatmap(
            @RequestHeader("X-MEMBER-ID") Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        WriteHistoryHeatmapResponse response =
                writeHistoryQueryService.getHeatmap(memberId, startDate, endDate);

        return ApiResponse.success(response);
    }
}
