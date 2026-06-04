package com.tilog.domain.post.service;

import com.tilog.domain.post.entity.Post;
import com.tilog.domain.post.dto.FeedPostResponse;
import com.tilog.global.security.SecurityUtil;
import com.tilog.domain.follow.repository.FollowRepository;
import com.tilog.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final FollowRepository followRepository;
    private final PostRepository postRepository;  // 2번 담당자 Repository

    /**
     * 팔로잉 피드 — 내가 팔로우한 사용자들의 TIL 최신순 조회
     * PostRepository에 아래 메서드 추가 필요 (2번 담당자에게 요청):
     *
     * Slice<Post> findByMember_IdInAndIsDeletedFalseOrderByCreatedAtDesc(
     *     Collection<Long> memberIds, Pageable pageable
     * );
     */
    public List<FeedPostResponse> getFollowingFeed(int page, int size) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 팔로잉 ID 목록 조회
        List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(currentMemberId);

        // 팔로잉이 없으면 빈 피드 반환
        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }

        Slice<Post> posts = postRepository
                .findByMember_IdInAndIsDeletedFalseOrderByCreatedAtDesc(
                        followingIds, PageRequest.of(page, size)
                );

        return posts.stream()
                .map(FeedPostResponse::from)
                .collect(Collectors.toList());
    }
}