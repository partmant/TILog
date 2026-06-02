package com.tilog.domain.follow.controller;

import com.tilog.domain.follow.dto.FollowResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /** 팔로우 */
    @PostMapping("/{targetMemberId}")
    public ResponseEntity<ApiResponse<FollowResponse>> follow(@PathVariable Long targetMemberId) {
        FollowResponse response = followService.follow(targetMemberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 언팔로우 */
    @DeleteMapping("/{targetMemberId}")
    public ResponseEntity<ApiResponse<FollowResponse>> unfollow(@PathVariable Long targetMemberId) {
        FollowResponse response = followService.unfollow(targetMemberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 팔로우 여부 확인 */
    @GetMapping("/{targetMemberId}")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(@PathVariable Long targetMemberId) {
        boolean following = followService.isFollowing(targetMemberId);
        return ResponseEntity.ok(ApiResponse.success(following));
    }
}
