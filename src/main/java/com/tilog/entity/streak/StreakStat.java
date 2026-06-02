package com.tilog.entity.streak;

import com.tilog.entity.Member;
import com.tilog.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "streak_stat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StreakStat extends BaseTimeEntity {
    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column(name = "total_written_days", nullable = false)
    private int totalWrittenDays;

    @Column(name = "last_written_date")
    private LocalDate lastWrittenDate;

    private StreakStat(Member member, LocalDate writtenDate) {
        this.member = member;
        this.currentStreak = 1;
        this.longestStreak = 1;
        this.totalWrittenDays = 1;
        this.lastWrittenDate = writtenDate;
    }

    public static StreakStat create(Member member, LocalDate writtenDate) {
        return new StreakStat(member, writtenDate);
    }

    public void update(LocalDate writtenDate) {
        if (lastWrittenDate == null) {
            currentStreak = 1;
            longestStreak = 1;
            totalWrittenDays = 1;
            lastWrittenDate = writtenDate;
            return;
        }

        if (lastWrittenDate.isEqual(writtenDate)) {
            return;
        }

        if (lastWrittenDate.plusDays(1).isEqual(writtenDate)) {
            currentStreak++;
        } else {
            currentStreak = 1;
        }

        longestStreak = Math.max(longestStreak, currentStreak);
        totalWrittenDays++;
        lastWrittenDate = writtenDate;
    }
}
