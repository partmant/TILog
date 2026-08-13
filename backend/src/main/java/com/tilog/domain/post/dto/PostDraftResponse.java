package com.tilog.domain.post.dto;

import com.tilog.domain.post.entity.Post;

import java.time.LocalDateTime;

// 내 임시저장(DRAFT) 게시글 목록 조회용 dto
public record PostDraftResponse(
        Long postId,
        String title,
        String difficulty,
        Integer studyTime,
        LocalDateTime updatedAt
) {
    public static PostDraftResponse from(Post post) {
        return new PostDraftResponse(
                post.getId(),
                post.getTitle(),
                post.getDifficulty() != null ? post.getDifficulty().name() : null,
                post.getStudyTime(),
                post.getUpdatedAt()
        );
    }
}
