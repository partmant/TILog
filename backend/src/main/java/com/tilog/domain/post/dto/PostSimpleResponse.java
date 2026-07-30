package com.tilog.domain.post.dto;

import com.tilog.domain.post.entity.Post;

public record PostSimpleResponse(   // 게시글 id와 제목만 담는 dto
        Long postId,
        String title
) {
    public static PostSimpleResponse from(Post post) {
        return new PostSimpleResponse(
                post.getId(),
                post.getTitle()
        );
    }
}