package com.tilog.repository.streak;

import com.tilog.entity.streak.StreakStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreakStatRepository extends JpaRepository<StreakStat, Long> {
}
