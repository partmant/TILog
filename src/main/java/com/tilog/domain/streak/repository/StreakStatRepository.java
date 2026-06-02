package com.tilog.domain.streak.repository;

import com.tilog.domain.streak.entity.StreakStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreakStatRepository extends JpaRepository<StreakStat, Long> {
}
