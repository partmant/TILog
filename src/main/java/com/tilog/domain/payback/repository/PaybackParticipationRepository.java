package com.tilog.domain.payback.repository;

import com.tilog.domain.payback.entity.PaybackParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaybackParticipationRepository extends JpaRepository<PaybackParticipation, Long> {
    @Query("""
        SELECT COUNT(p) > 0
        FROM PaybackParticipation p
        WHERE p.memberId = :memberId
          AND p.paybackPolicy.paybackPolicyId = :policyId
          AND p.participationMonth = :month
        """)
    boolean existsByMemberPolicyMonth(
            @Param("memberId") Long memberId,
            @Param("policyId") Long policyId,
            @Param("month") String month
    );

    Optional<PaybackParticipation> findByMemberIdAndParticipationMonth(
            Long memberId,
            String participationMonth
    );
}
