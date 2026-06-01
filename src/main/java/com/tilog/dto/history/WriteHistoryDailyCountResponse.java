package com.tilog.dto.history;

import java.time.LocalDate;

public record WriteHistoryDailyCountResponse(
        LocalDate date,
        int writeCount
) {
}
