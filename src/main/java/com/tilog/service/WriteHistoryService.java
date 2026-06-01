package com.tilog.service;

import com.tilog.dto.history.WriteHistoryRequest;
import com.tilog.dto.history.WriteHistoryResponse;
import com.tilog.entity.WriteHistory;
import com.tilog.repository.WriteHistoryRepository;
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
    private final StreakStatService streakStatService;

    @Transactional
    public WriteHistoryResponse recordWriteHistory(WriteHistoryRequest request) {
        LocalDate today = LocalDate.now();

        WriteHistory writeHistory = writeHistoryRepository
                .findByMemberIdAndWrittenDate(request.memberId(), today)
                .map(existingHistory -> {
                    existingHistory.increaseCount();
                    return existingHistory;
                })
                .orElseGet(() -> writeHistoryRepository.save(
                        WriteHistory.create(request.memberId(), today)
                ));

        streakStatService.updateStreak(request.memberId(), today);

        return WriteHistoryResponse.from(writeHistory);
    }
}
