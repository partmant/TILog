package com.tilog.domain.payback.repository;

import com.tilog.domain.payback.entity.PaybackPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PaybackPolicyRepository extends JpaRepository<PaybackPolicy, Long> {
    Optional<PaybackPolicy> findFirstByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
            LocalDate todayForStart,
            LocalDate todayForEnd
    );
}
