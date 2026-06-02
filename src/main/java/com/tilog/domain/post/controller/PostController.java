package com.tilog.domain.post.controller;

import com.tilog.domain.post.dto.PostCommandDto;
import com.tilog.domain.post.dto.PostQueryDto;
import com.tilog.domain.post.service.PostService;
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
        return postService.getPostList();
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public PostQueryDto.DetailResponse getPostDetail(
            @PathVariable("postId") Long postId,
            @RequestParam(value = "increaseViewCount", defaultValue = "true") boolean increaseViewCount
    ) {
        return postService.getPostDetail(postId, increaseViewCount);
    }

    // 게시글 작성
    @PostMapping
    public Long createPost(@RequestBody PostCommandDto.Create request) {
        return postService.createPost(request);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public Long updatePost(@PathVariable("postId") Long postId, @RequestBody PostCommandDto.Update request) {
        return postService.updatePost(postId, request);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);
    }
}
