package com.tilog.domain.post.controller;

import com.tilog.domain.post.dto.PostCommandDto;
import com.tilog.domain.post.dto.PostQueryDto;
import com.tilog.domain.post.dto.PostSimpleResponse;
import com.tilog.domain.post.service.PostService;
import com.tilog.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 게시글 요청 처리 컨트롤러

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public List<PostQueryDto.ListResponse> getPostList() {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        return postService.getPostList(loginMemberId);
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public PostQueryDto.DetailResponse getPostDetail(
            @PathVariable("postId") Long postId,
            @RequestParam(value = "increaseViewCount", defaultValue = "true") boolean increaseViewCount
    ) {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        return postService.getPostDetail(postId, increaseViewCount, loginMemberId);
    }

    // 게시글 핵심 요약 생성
    @PostMapping("/{postId}/summary")
    public PostQueryDto.SummaryResponse summarizePost(@PathVariable("postId") Long postId) {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        return postService.summarizePost(postId, loginMemberId);
    }

    // 게시글 작성
    @PostMapping
    public Long createPost(@RequestBody PostCommandDto.Create request) {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        return postService.createPost(request, loginMemberId);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public Long updatePost(@PathVariable("postId") Long postId, @RequestBody PostCommandDto.Update request) {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        return postService.updatePost(postId, request, loginMemberId);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable("postId") Long postId) {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        postService.deletePost(postId, loginMemberId);
    }

    // 🔥 추가: 내 게시글 간단 목록 조회
    @GetMapping("/me/simple")
    public List<PostSimpleResponse> getMySimplePosts() {
        Long loginMemberId = SecurityUtil.getCurrentMemberId();
        return postService.getMySimplePosts(loginMemberId);
    }
}
