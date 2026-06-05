package com.tilog.domain.streak.dto;

import com.tilog.domain.streak.entity.StreakStat;

import java.time.LocalDate;

public record StreakStatResponse(
        Long memberId,
        int currentStreak,
        int longestStreak,
        int totalWrittenDays,
        int totalTilCount,
        LocalDate lastWrittenDate
) {
    public static StreakStatResponse from(StreakStat streakStat, int totalTilCount) {
        return new StreakStatResponse(
                streakStat.getMemberId(),
                streakStat.getCurrentStreak(),
                streakStat.getLongestStreak(),
                streakStat.getTotalWrittenDays(),
                totalTilCount,
                streakStat.getLastWrittenDate()
        );
    }

    public static StreakStatResponse empty(Long memberId) {
        return new StreakStatResponse(
                memberId,
                0,
                0,
                0,
                0,
                null
        );
    }
}
