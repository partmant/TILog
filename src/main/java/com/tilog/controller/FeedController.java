package com.tilog.controller;

import com.tilog.dto.feed.FeedPostResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
