package com.tilog.repository;

import com.tilog.entity.TilPost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TilPostRepository extends JpaRepository<TilPost, Long>, TilPostRepositoryCustom {

    /**
     * 주간 요약 — 총 게시글 수, 총 학습 시간(분)
     * returns Object[]{Long count, Long totalStudyTime}
     */
    @Query("SELECT COUNT(p), COALESCE(SUM(p.studyTime), 0) " +
           "FROM TilPost p " +
           "WHERE p.member.memberId = :memberId " +
           "AND p.createdAt BETWEEN :from AND :to " +
           "AND p.deleted = false")
    List<Object[]> findWeeklySummary(@Param("memberId") Long memberId,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    /**
     * 난이도별 게시글 수
     * returns List<Object[]{Difficulty, Long count}>
     */
    @Query("SELECT p.difficulty, COUNT(p) " +
           "FROM TilPost p " +
           "WHERE p.member.memberId = :memberId " +
           "AND p.createdAt BETWEEN :from AND :to " +
           "AND p.deleted = false " +
           "GROUP BY p.difficulty")
    List<Object[]> findDifficultyDistribution(@Param("memberId") Long memberId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    /**
     * 태그별 게시글 수 (기술 스택 분포 집계용)
     * returns List<Object[]{String tagName, Long count}>
     */
    @Query("SELECT pt.tag.name, COUNT(p) " +
           "FROM TilPost p " +
           "JOIN TilPostTag pt ON pt.post = p " +
           "WHERE p.member.memberId = :memberId " +
           "AND p.createdAt BETWEEN :from AND :to " +
           "AND p.deleted = false " +
           "GROUP BY pt.tag.name")
    List<Object[]> findTagDistribution(@Param("memberId") Long memberId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);
}