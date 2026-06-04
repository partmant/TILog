package com.tilog.domain.payback.repository;

import com.tilog.domain.payback.entity.PaybackParticipation;
import com.tilog.domain.payback.entity.PaybackResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PaybackParticipationRepository extends JpaRepository<PaybackParticipation, Long> {
    boolean existsBySubscription_Id(Long subscriptionId);

    @Query("""
            SELECT p
            FROM PaybackParticipation p
            JOIN FETCH p.paybackPolicy
            JOIN FETCH p.subscription
            WHERE p.memberId = :memberId
              AND p.resultStatus = :resultStatus
              AND p.periodStartDate <= :today
              AND p.periodEndDate >= :today
            """)
    Optional<PaybackParticipation> findCurrentParticipation(
            @Param("memberId") Long memberId,
            @Param("resultStatus") PaybackResultStatus resultStatus,
            @Param("today") LocalDate today
    );
}
