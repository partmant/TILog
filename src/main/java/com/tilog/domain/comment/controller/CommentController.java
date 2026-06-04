package com.tilog.domain.comment.controller;

import com.tilog.domain.comment.dto.CommentCreateRequest;
import com.tilog.domain.comment.dto.CommentResponse;
import com.tilog.domain.comment.dto.CommentUpdateRequest;
import com.tilog.global.response.ApiResponse;
import com.tilog.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 댓글 작성 / 대댓글 작성 (parentCommentId 포함 시 대댓글) */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {

        CommentResponse response = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /** 댓글 수정 */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {

        CommentResponse response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 댓글 삭제 */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.<Void>success("댓글이 삭제되었습니다."));
    }

    /** 게시글 댓글 목록 조회 */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long postId) {
        List<CommentResponse> response = commentService.getComments(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** 대댓글 목록 조회 */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(@PathVariable Long commentId) {
        List<CommentResponse> response = commentService.getReplies(commentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
