package com.tilog.dto.streak;

import com.tilog.entity.streak.StreakStat;

import java.time.LocalDate;

public record StreakStatResponse(
        Long memberId,
        int currentStreak,
        int longestStreak,
        int totalWrittenDays,
        LocalDate lastWrittenDate
) {
    public static StreakStatResponse from(StreakStat streakStat) {
        return new StreakStatResponse(
                streakStat.getMemberId(),
                streakStat.getCurrentStreak(),
                streakStat.getLongestStreak(),
                streakStat.getTotalWrittenDays(),
                streakStat.getLastWrittenDate()
        );
    }

    public static StreakStatResponse empty(Long memberId) {
        return new StreakStatResponse(
                memberId,
                0,
                0,
                0,
                null
        );
    }
}
