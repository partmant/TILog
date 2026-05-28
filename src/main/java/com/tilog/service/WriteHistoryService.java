package com.tilog.service;

import com.tilog.dto.history.WriteHistoryRequest;
import com.tilog.dto.history.WriteHistoryResponse;
import com.tilog.entity.WriteHistory;
import com.tilog.repository.WriteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WriteHistoryService {
    private final WriteHistoryRepository writeHistoryRepository;

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

        return WriteHistoryResponse.from(writeHistory);
    }
}
