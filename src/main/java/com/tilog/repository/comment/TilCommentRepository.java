package com.tilog.repository.comment;

import com.tilog.entity.comment.TilComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TilCommentRepository extends JpaRepository<TilComment, Long> {

    // 삭제되지 않은 댓글 단건 조회
    @Query("SELECT c FROM TilComment c WHERE c.commentId = :commentId AND c.isDeleted = false")
    Optional<TilComment> findActiveById(@Param("commentId") Long commentId);

    // 게시글의 최상위 댓글 목록 (최신순)
    @Query("SELECT c FROM TilComment c WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<TilComment> findTopLevelCommentsByPostId(@Param("postId") Long postId);

    // 특정 댓글의 대댓글 목록
    @Query("SELECT c FROM TilComment c WHERE c.parentComment.commentId = :parentCommentId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<TilComment> findRepliesByParentId(@Param("parentCommentId") Long parentCommentId);
}
