package com.tilog.domain.like.controller;

import com.tilog.domain.like.dto.LikeResponse;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /** 좋아요 등록 */
    @PostMapping
    public ResponseEntity<ApiResponse<LikeResponse>> like(@PathVariable Long postId) {
        LikeResponse response = likeService.like(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 좋아요 취소 */
    @DeleteMapping
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(@PathVariable Long postId) {
        LikeResponse response = likeService.unlike(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 좋아요 정보 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<LikeResponse>> getLikeInfo(@PathVariable Long postId) {
        LikeResponse response = likeService.getLikeInfo(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
