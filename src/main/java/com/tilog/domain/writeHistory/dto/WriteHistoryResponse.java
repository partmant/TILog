package com.tilog.domain.writeHistory.dto;

import com.tilog.domain.writeHistory.entity.WriteHistory;

import java.time.LocalDate;

public record WriteHistoryResponse(
        Long historyId,
        Long memberId,
        LocalDate writtenDate,
        int writeCount
) {
    public static WriteHistoryResponse from(WriteHistory writeHistory) {
        return new WriteHistoryResponse(
                writeHistory.getId(),
                writeHistory.getMemberId(),
                writeHistory.getWrittenDate(),
                writeHistory.getWriteCount()
        );
    }
}