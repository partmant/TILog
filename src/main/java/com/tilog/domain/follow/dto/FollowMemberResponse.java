package com.tilog.domain.follow.dto;

import com.tilog.domain.follow.entity.Follow;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FollowMemberResponse {

    private final Long memberId;
    private final String nickname;
    private final LocalDateTime followedAt;

    private FollowMemberResponse(Long memberId, String nickname, LocalDateTime followedAt) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.followedAt = followedAt;
    }

    // 팔로워 목록 (나를 팔로우한 사람 = follower)
    public static FollowMemberResponse fromFollower(Follow follow) {
        return new FollowMemberResponse(
                follow.getFollower().getId(),
                follow.getFollower().getNickname(),
                follow.getCreatedAt()
        );
    }

    // 팔로잉 목록 (내가 팔로우한 사람 = following)
    public static FollowMemberResponse fromFollowing(Follow follow) {
        return new FollowMemberResponse(
                follow.getFollowing().getId(),
                follow.getFollowing().getNickname(),
                follow.getCreatedAt()
        );
    }
}
