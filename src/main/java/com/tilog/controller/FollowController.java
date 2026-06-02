package com.tilog.controller;

import com.tilog.dto.follow.FollowMemberResponse;
import com.tilog.dto.follow.FollowResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** 팔로워 목록 — 나를 팔로우한 사람들 */
    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<FollowMemberResponse>>> getFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<FollowMemberResponse> response = followService.getFollowers(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 팔로잉 목록 — 내가 팔로우한 사람들 */
    @GetMapping("/followings")
    public ResponseEntity<ApiResponse<List<FollowMemberResponse>>> getFollowings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<FollowMemberResponse> response = followService.getFollowings(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}