package com.tilog.service;

import com.tilog.dto.follow.FollowResponse;
import com.tilog.entity.Follow;
import com.tilog.entity.Member;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.repository.FollowRepository;
import com.tilog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    /** 팔로우 */
    @Transactional
    public FollowResponse follow(Long targetMemberId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 자기 자신 팔로우 방지
        if (currentMemberId.equals(targetMemberId)) {
            throw new CustomException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        // 이미 팔로우 여부 확인
        if (followRepository.existsByFollower_IdAndFollowing_Id(currentMemberId, targetMemberId)) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
        }

        Member follower = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member following = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.TARGET_MEMBER_NOT_FOUND));

        followRepository.save(Follow.create(follower, following));

        long followerCount = followRepository.countByFollowing_Id(targetMemberId);
        return FollowResponse.of(targetMemberId, true, followerCount);
    }

    /** 언팔로우 */
    @Transactional
    public FollowResponse unfollow(Long targetMemberId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        Follow follow = followRepository
                .findByFollower_IdAndFollowing_Id(currentMemberId, targetMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);

        long followerCount = followRepository.countByFollowing_Id(targetMemberId);
        return FollowResponse.of(targetMemberId, false, followerCount);
    }

    /** 팔로우 여부 확인 */
    public boolean isFollowing(Long targetMemberId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return followRepository.existsByFollower_IdAndFollowing_Id(currentMemberId, targetMemberId);
    }
}
