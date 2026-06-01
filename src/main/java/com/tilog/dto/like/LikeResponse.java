package com.tilog.dto.like;

import lombok.Getter;

@Getter
public class LikeResponse {

    private final Long postId;
    private final long likeCount;
    private final boolean liked;

    private LikeResponse(Long postId, long likeCount, boolean liked) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.liked = liked;
    }

    public static LikeResponse of(Long postId, long likeCount, boolean liked) {
        return new LikeResponse(postId, likeCount, liked);
    }
}
