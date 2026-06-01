package com.tilog.service;

import com.tilog.dto.comment.CommentCreateRequest;
import com.tilog.dto.comment.CommentResponse;
import com.tilog.dto.comment.CommentUpdateRequest;
import com.tilog.entity.Member;
import com.tilog.entity.Post;
import com.tilog.entity.TilComment;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.SecurityUtil;
import com.tilog.repository.MemberRepository;
import com.tilog.repository.PostRepository;
import com.tilog.repository.TilCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final TilCommentRepository commentRepository;
    private final PostRepository postRepository;       // 2번 담당자 Repository
    private final MemberRepository memberRepository;        // 1번 담당자 Repository

    /** 댓글 작성 (대댓글 포함) */
    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        TilComment comment;

        if (request.getParentCommentId() != null) {
            // 대댓글: 부모 댓글 검증
            TilComment parentComment = commentRepository.findActiveById(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PARENT_COMMENT_NOT_FOUND));

            // 부모 댓글이 같은 게시글 소속인지 확인
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new CustomException(ErrorCode.PARENT_COMMENT_POST_MISMATCH);
            }

            // 대댓글에 대한 대댓글 방지 (1단계 깊이만 허용)
            if (parentComment.getParentComment() != null) {
                throw new CustomException(ErrorCode.PARENT_COMMENT_NOT_FOUND);
            }

            comment = TilComment.createReply(post, member, request.getContent(), parentComment);
        } else {
            // 일반 댓글
            comment = TilComment.create(post, member, request.getContent());
        }

        return CommentResponse.from(commentRepository.save(comment));
    }

    /** 댓글 수정 */
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        TilComment comment = commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isOwner(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.update(request.getContent());
        return CommentResponse.from(comment);
    }

    /** 댓글 삭제 (소프트 딜리트) */
    @Transactional
    public void deleteComment(Long commentId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        TilComment comment = commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isOwner(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.delete();
    }

    /** 게시글의 댓글 목록 조회 */
    public List<CommentResponse> getComments(Long postId) {
        return commentRepository.findTopLevelCommentsByPostId(postId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    /** 대댓글 목록 조회 */
    public List<CommentResponse> getReplies(Long commentId) {
        commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        return commentRepository.findRepliesByParentId(commentId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }
}
