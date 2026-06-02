package com.tilog.domain.payback.repository;

import com.tilog.domain.payback.entity.PaybackParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaybackParticipationRepository extends JpaRepository<PaybackParticipation, Long> {
    boolean existsByMemberIdAndPaybackPolicyPaybackPolicyIdAndParticipationMonth(
            Long memberId,
            Long paybackPolicyId,
            String participationMonth
    );

    Optional<PaybackParticipation> findByMemberIdAndParticipationMonth(
            Long memberId,
            String participationMonth
    );
}
