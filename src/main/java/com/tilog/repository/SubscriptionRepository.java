package com.tilog.repository;

import com.tilog.entity.Subscription;
import com.tilog.entity.enums.SubscriptionStatus;
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

    // 만료 처리가 필요한 구독 조회 (ACTIVE 상태이면서 endedAt이 현재 이전)
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endedAt < :now")
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
}
