package com.tilog.service;

import com.tilog.dto.follow.FollowMemberResponse;
import com.tilog.dto.follow.FollowResponse;
import com.tilog.entity.Follow;
import com.tilog.entity.Member;
import com.tilog.entity.notification.NotificationType;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.repository.follow.FollowRepository;
import com.tilog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    /** 팔로우 */
    @Transactional
    public FollowResponse follow(Long targetMemberId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        if (currentMemberId.equals(targetMemberId)) {
            throw new CustomException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        if (followRepository.existsByFollower_IdAndFollowing_Id(currentMemberId, targetMemberId)) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
        }

        Member follower = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member following = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.TARGET_MEMBER_NOT_FOUND));

        followRepository.save(Follow.create(follower, following));

        // 팔로우 알림 발송
        notificationService.send(targetMemberId, currentMemberId, NotificationType.FOLLOW, null, null);

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

    /** 팔로워 목록 — 나를 팔로우한 사람들 */
    public List<FollowMemberResponse> getFollowers(int page, int size) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Slice<Follow> follows = followRepository
                .findByFollowing_IdOrderByCreatedAtDesc(currentMemberId, PageRequest.of(page, size));

        return follows.stream()
                .map(FollowMemberResponse::fromFollower)
                .collect(Collectors.toList());
    }

    /** 팔로잉 목록 — 내가 팔로우한 사람들 */
    public List<FollowMemberResponse> getFollowings(int page, int size) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Slice<Follow> follows = followRepository
                .findByFollower_IdOrderByCreatedAtDesc(currentMemberId, PageRequest.of(page, size));

        return follows.stream()
                .map(FollowMemberResponse::fromFollowing)
                .collect(Collectors.toList());
    }
}