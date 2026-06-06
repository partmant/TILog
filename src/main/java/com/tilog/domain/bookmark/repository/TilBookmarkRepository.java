package com.tilog.domain.bookmark.repository;

import com.tilog.domain.bookmark.entity.TilBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TilBookmarkRepository extends JpaRepository<TilBookmark, Long> {

    boolean existsByPost_IdAndMember_Id(Long postId, Long memberId);

    Optional<TilBookmark> findByPost_IdAndMember_Id(Long postId, Long memberId);

    /** 특정 회원이 즐겨찾기한 postId 목록 조회 (배치 체크용) */
    @Query("SELECT b.post.id FROM TilBookmark b WHERE b.member.id = :memberId AND b.post.id IN :postIds")
    Set<Long> findBookmarkedPostIdsByMemberIdAndPostIds(
            @Param("memberId") Long memberId,
            @Param("postIds") List<Long> postIds
    );

    /** 즐겨찾기 목록 조회 (최신 즐겨찾기 순, 페이지네이션) */
    Page<TilBookmark> findByMember_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    /** 즐겨찾기 전체 조회 (검색/정렬용 인메모리 처리) */
    List<TilBookmark> findByMember_IdOrderByCreatedAtDesc(Long memberId);
}
