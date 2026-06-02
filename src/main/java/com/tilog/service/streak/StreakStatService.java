package com.tilog.service.streak;

import com.tilog.dto.streak.StreakStatResponse;
import com.tilog.entity.Member;
import com.tilog.entity.streak.StreakStat;
import com.tilog.repository.streak.StreakStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakStatService {
    private final StreakStatRepository streakStatRepository;

    @Transactional
    public void updateStreak(Member member, LocalDate writtenDate) {
        streakStatRepository.findById(member.getId())
                .ifPresentOrElse(
                        streakStat -> streakStat.update(writtenDate),
                        () -> streakStatRepository.save(StreakStat.create(member, writtenDate))
                );
    }

    @Transactional(readOnly = true)
    public StreakStatResponse getStreak(Long memberId) {
        return streakStatRepository.findById(memberId)
                .map(StreakStatResponse::from)
                .orElseGet(() -> StreakStatResponse.empty(memberId));
    }
}
