package com.tilog.domain.post.repository;

import com.tilog.domain.member.entity.Member;
import com.tilog.domain.post.entity.Post;
import com.tilog.domain.post.entity.Visibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// 게시글 DB 접근 Repository

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    // 삭제되지 않았고, 임시저장이 아닌 게시글 목록 조회
    List<Post> findByIsDeletedFalseAndVisibilityNot(Visibility visibility);

    // 특정 회원이 작성한 삭제되지 않은 게시글 목록 조회
    // PUBLIC, PRIVATE, DRAFT 모두 포함
    List<Post> findByMemberAndIsDeletedFalse(Member member);

    // 특정 회원의 임시저장 게시글 목록 조회
    List<Post> findByMemberAndVisibilityAndIsDeletedFalse(Member member, Visibility visibility);

    // 팔로잉 피드용

    // 마이페이지 상단 통계 - 회원의 전체 TIL 수 (삭제 제외, DRAFT 포함)
    @Query("SELECT COUNT(p) FROM Post p WHERE p.member.id = :memberId AND p.isDeleted = false")
    int countTotalTilsByMember(@Param("memberId") Long memberId);

    Slice<Post> findByMember_IdInAndIsDeletedFalseOrderByCreatedAtDesc(
            Collection<Long> memberIds, Pageable pageable
    );

    // 특정 회원의 공개 TIL 목록 (PUBLIC만, 최신순)
    Slice<Post> findByMember_IdAndVisibilityAndIsDeletedFalseOrderByCreatedAtDesc(
            Long memberId, Visibility visibility, Pageable pageable
    );

    /**
     * 주간 요약 — 총 게시글 수, 총 학습 시간(분)
     * returns Object[]{Long count, Long totalStudyTime}
     */
    @Query("SELECT COUNT(p), COALESCE(SUM(p.studyTime), 0) " +
        "FROM Post p " +
        "WHERE p.member.id = :memberId " +
        "AND p.createdAt BETWEEN :from AND :to " +
        "AND p.isDeleted = false")
    List<Object[]> findWeeklySummary(@Param("memberId") Long memberId,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    /**
     * 난이도별 게시글 수
     * returns List<Object[]{Difficulty, Long count}>
     */
    @Query("SELECT p.difficulty, COUNT(p) " +
        "FROM Post p " +
        "WHERE p.member.id = :memberId " +
        "AND p.createdAt BETWEEN :from AND :to " +
        "AND p.isDeleted = false " +
        "GROUP BY p.difficulty")
    List<Object[]> findDifficultyDistribution(@Param("memberId") Long memberId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    /**
     * 태그별 게시글 수 (기술 스택 분포 집계용)
     * returns List<Object[]{String tagName, Long count}>
     */
    @Query("SELECT pt.tag.name, COUNT(p) " +
        "FROM Post p " +
        "JOIN PostTag pt ON pt.post = p " +
        "WHERE p.member.id = :memberId " +
        "AND p.createdAt BETWEEN :from AND :to " +
        "AND p.isDeleted = false " +
        "GROUP BY pt.tag.name")
    List<Object[]> findTagDistribution(@Param("memberId") Long memberId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    /**
     * 누적 요약 — 전체 기간 게시글 수, 학습시간, 난이도 분포
     * returns Object[]{Long count, Long totalStudyTime}
     */
    @Query("SELECT COUNT(p), COALESCE(SUM(p.studyTime), 0) " +
        "FROM Post p " +
        "WHERE p.member.id = :memberId " +
        "AND p.isDeleted = false")
    List<Object[]> findCumulativeSummary(@Param("memberId") Long memberId);

    /**
     * 누적 난이도별 게시글 수
     * returns List<Object[]{Difficulty, Long count}>
     */
    @Query("SELECT p.difficulty, COUNT(p) " +
        "FROM Post p " +
        "WHERE p.member.id = :memberId " +
        "AND p.isDeleted = false " +
        "GROUP BY p.difficulty")
    List<Object[]> findCumulativeDifficultyDistribution(@Param("memberId") Long memberId);

    /**
     * 누적 태그별 게시글 수
     * returns List<Object[]{String tagName, Long count}>
     */
    @Query("SELECT pt.tag.name, COUNT(p) " +
        "FROM Post p " +
        "JOIN PostTag pt ON pt.post = p " +
        "WHERE p.member.id = :memberId " +
        "AND p.isDeleted = false " +
        "GROUP BY pt.tag.name")
    List<Object[]> findCumulativeTagDistribution(@Param("memberId") Long memberId);

    List<Post> findByMember_IdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId); // 특정 회원의 삭제되지 않은 게시글 목록 (최신순 정렬)

    // 삭제 여부와 무관하게 특정 회원이 작성한 모든 게시글 (데모 계정 데이터 초기화용)
    List<Post> findByMember_Id(Long memberId);
}
