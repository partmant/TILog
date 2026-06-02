package com.tilog.service.WriteHistory;

import com.tilog.dto.writeHistory.WriteHistoryRequest;
import com.tilog.dto.writeHistory.WriteHistoryResponse;
import com.tilog.entity.Member;
import com.tilog.entity.writeHistory.WriteHistory;
import com.tilog.repository.MemberRepository;
import com.tilog.repository.writeHistory.WriteHistoryRepository;
import com.tilog.service.streak.StreakStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// 작성 이력 기록
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WriteHistoryService {
    private final WriteHistoryRepository writeHistoryRepository;
    private final MemberRepository memberRepository;
    private final StreakStatService streakStatService;

    @Transactional
    public WriteHistoryResponse recordWriteHistory(WriteHistoryRequest request) {
        LocalDate today = LocalDate.now();

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        WriteHistory writeHistory = writeHistoryRepository
                .findByMember_IdAndWrittenDate(member.getId(), today)
                .map(existingHistory -> {
                    existingHistory.increaseCount();
                    return existingHistory;
                })
                .orElseGet(() -> writeHistoryRepository.save(
                        WriteHistory.create(member, today)
                ));

        streakStatService.updateStreak(member, today);

        return WriteHistoryResponse.from(writeHistory);
    }
}
