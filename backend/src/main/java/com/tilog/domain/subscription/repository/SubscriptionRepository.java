package com.tilog.domain.subscription.repository;

import com.tilog.domain.subscription.entity.Subscription;
import com.tilog.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // 특정 회원의 활성 구독 조회 (가장 최근 것)
    Optional<Subscription> findTopByMemberIdAndStatusOrderByStartedAtDesc(
            Long memberId, SubscriptionStatus status);

    // 특정 회원의 모든 구독 이력 조회
    List<Subscription> findByMemberIdOrderByStartedAtDesc(Long memberId);

    // 특정 회원의 현재 유효한 구독을 조회 (ACTIVE 또는 CANCEL_RESERVED)
    @Query("""
            SELECT s
            FROM Subscription s
            WHERE s.member.id = :memberId
              AND s.status IN ('ACTIVE', 'CANCEL_RESERVED')
              AND s.startedAt <= :now
              AND s.endedAt >= :now
            ORDER BY s.startedAt DESC
            """)
    List<Subscription> findCurrentActiveSubscriptions(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 특정 회원의 ACTIVE 상태인 구독 전체 조회 (취소 시 모두 처리용)
    @Query("""
            SELECT s
            FROM Subscription s
            WHERE s.member.id = :memberId
              AND s.status = 'ACTIVE'
            """)
    List<Subscription> findAllActiveByMemberId(@Param("memberId") Long memberId);

    // 만료 처리가 필요한 구독 조회 (ACTIVE 또는 CANCEL_RESERVED 상태이면서 endedAt이 현재 이전)
    // JOIN FETCH로 member 즉시 로딩 (스케줄러에서 N+1 방지)
    @Query("""
            SELECT s
            FROM Subscription s
            JOIN FETCH s.member
            WHERE s.status IN ('ACTIVE', 'CANCEL_RESERVED')
              AND s.endedAt < :now
            """)
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
}
