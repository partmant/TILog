package com.tilog.service;

import com.tilog.dto.streak.StreakStatResponse;
import com.tilog.entity.StreakStat;
import com.tilog.repository.StreakStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakStatService {
    private final StreakStatRepository streakStatRepository;

    @Transactional
    public void updateStreak(Long memberId, LocalDate writtenDate) {
        streakStatRepository.findById(memberId)
                .ifPresentOrElse(
                        streakStat -> streakStat.update(writtenDate),
                        () -> streakStatRepository.save(StreakStat.create(memberId, writtenDate))
                );
    }

    @Transactional(readOnly = true)
    public StreakStatResponse getStreak(Long memberId) {
        // TODO: 인증 기능 구현 후 로그인 사용자 ID 기준으로 처리
        return streakStatRepository.findById(memberId)
                .map(StreakStatResponse::from)
                .orElseGet(() -> StreakStatResponse.empty(memberId));
    }
}
