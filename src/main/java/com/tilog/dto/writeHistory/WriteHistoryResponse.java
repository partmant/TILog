package com.tilog.dto.writeHistory;

import com.tilog.entity.writeHistory.WriteHistory;

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