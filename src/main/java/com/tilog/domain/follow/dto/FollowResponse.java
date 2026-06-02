package com.tilog.domain.follow.dto;

import lombok.Getter;

@Getter
public class FollowResponse {

    private final Long followingId;
    private final boolean following;
    private final long followerCount;

    private FollowResponse(Long followingId, boolean following, long followerCount) {
        this.followingId = followingId;
        this.following = following;
        this.followerCount = followerCount;
    }

    public static FollowResponse of(Long followingId, boolean following, long followerCount) {
        return new FollowResponse(followingId, following, followerCount);
    }
}
