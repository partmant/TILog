package com.tilog.domain.like.service;

import com.tilog.domain.like.dto.LikeResponse;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.like.entity.TilPostLike;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.domain.member.repository.MemberRepository;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.domain.like.repository.TilPostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final TilPostLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /** 좋아요 등록 */
    @Transactional
    public LikeResponse like(Long postId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        if (likeRepository.existsByPost_IdAndMember_Id(postId, memberId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        likeRepository.save(TilPostLike.create(post, member));

        long likeCount = likeRepository.countByPost_Id(postId);
        return LikeResponse.of(postId, likeCount, true);
    }

    /** 좋아요 취소 */
    @Transactional
    public LikeResponse unlike(Long postId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        TilPostLike like = likeRepository
                .findByPost_IdAndMember_Id(postId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);

        long likeCount = likeRepository.countByPost_Id(postId);
        return LikeResponse.of(postId, likeCount, false);
    }

    /** 좋아요 수 조회 */
    public LikeResponse getLikeInfo(Long postId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        boolean liked = likeRepository.existsByPost_IdAndMember_Id(postId, memberId);
        long likeCount = likeRepository.countByPost_Id(postId);
        return LikeResponse.of(postId, likeCount, liked);
    }
}
