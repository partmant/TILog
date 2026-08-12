package com.tilog.domain.like.repository;

import com.tilog.domain.like.entity.TilPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TilPostLikeRepository extends JpaRepository<TilPostLike, Long> {

    boolean existsByPost_IdAndMember_Id(Long postId, Long memberId);

    Optional<TilPostLike> findByPost_IdAndMember_Id(Long postId, Long memberId);

    long countByPost_Id(Long postId);

    // 게시글 하드 삭제 전 좋아요 일괄 삭제 (데모 계정 데이터 초기화용)
    @Modifying
    @Query("DELETE FROM TilPostLike l WHERE l.post.id = :postId")
    void deleteByPost_Id(@Param("postId") Long postId);
}
