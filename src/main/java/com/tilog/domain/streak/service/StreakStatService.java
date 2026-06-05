package com.tilog.domain.streak.service;

import com.tilog.domain.streak.dto.StreakStatResponse;
import com.tilog.domain.member.entity.Member;
import com.tilog.domain.post.repository.PostRepository;
import com.tilog.domain.streak.entity.StreakStat;
import com.tilog.domain.streak.repository.StreakStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakStatService {
    private final StreakStatRepository streakStatRepository;
    private final PostRepository postRepository;

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
        int totalTilCount = postRepository.countTotalTilsByMember(memberId);

        return streakStatRepository.findById(memberId)
                .map(streakStat -> StreakStatResponse.from(streakStat, totalTilCount))
                .orElseGet(() -> StreakStatResponse.empty(memberId));
    }
}
