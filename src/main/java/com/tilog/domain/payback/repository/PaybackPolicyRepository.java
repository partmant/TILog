package com.tilog.domain.payback.repository;

import com.tilog.domain.payback.entity.PaybackPolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaybackPolicyRepository extends JpaRepository<PaybackPolicy, Long> {
    @Query("""
            SELECT p
            FROM PaybackPolicy p
            WHERE p.active = true
              AND p.startDate <= :today
              AND (p.endDate IS NULL OR p.endDate >= :today)
            ORDER BY p.startDate DESC
            """)
    List<PaybackPolicy> findActivePolicies(
            @Param("today") LocalDate today,
            Pageable pageable
    );
}
