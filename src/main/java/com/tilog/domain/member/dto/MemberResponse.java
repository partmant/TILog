package com.tilog.domain.member.dto;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

public record MemberResponse(
        Long memberId,
        String email,
        String nickname,
        MemberRole role,
        LocalDateTime createdAt,
        String profileImageUrl
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                member.getCreatedAt(),
                member.getProfileImageUrl()
        );
    }
}
