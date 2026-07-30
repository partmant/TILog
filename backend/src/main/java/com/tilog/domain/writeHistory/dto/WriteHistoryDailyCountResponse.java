package com.tilog.domain.writeHistory.dto;

import java.time.LocalDate;

public record WriteHistoryDailyCountResponse(
        LocalDate date,
        int writeCount
) {
}
