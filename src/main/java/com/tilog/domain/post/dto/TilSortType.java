package com.tilog.domain.post.dto;

public enum TilSortType {
    LATEST,   // created_at DESC (기본값)
    LIKES,    // 좋아요 수 DESC
    COMMENTS  // 댓글 수 DESC
}