package com.tilog.domain.post.controller;

import com.tilog.domain.post.dto.FeedPostResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.post.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /** 팔로잉 피드 — 팔로우한 사용자의 TIL 최신순 */
    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<FeedPostResponse>>> getFollowingFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<FeedPostResponse> response = feedService.getFollowingFeed(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 특정 회원의 공개 TIL 목록 */
    @GetMapping("/members/{memberId}/tils")
    public ResponseEntity<ApiResponse<List<FeedPostResponse>>> getMemberTils(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<FeedPostResponse> response = feedService.getMemberTils(memberId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
