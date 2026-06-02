package com.tilog.service;

import com.tilog.dto.like.LikeResponse;
import com.tilog.entity.Member;
import com.tilog.entity.notification.NotificationType;
import com.tilog.entity.Post;
import com.tilog.entity.TilPostLike;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.repository.MemberRepository;
import com.tilog.repository.PostRepository;
import com.tilog.repository.TilPostLikeRepository;
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
    private final NotificationService notificationService;

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

        // 좋아요 알림 발송 (TIL 작성자에게)
        notificationService.send(
                post.getMember().getId(),
                memberId,
                NotificationType.LIKE,
                postId,
                "TIL_POST"
        );

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

    /** 좋아요 정보 조회 */
    public LikeResponse getLikeInfo(Long postId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        boolean liked = likeRepository.existsByPost_IdAndMember_Id(postId, memberId);
        long likeCount = likeRepository.countByPost_Id(postId);
        return LikeResponse.of(postId, likeCount, liked);
    }
}