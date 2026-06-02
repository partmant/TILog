package com.tilog.dto.writeHistory;

import java.time.LocalDate;

public record WriteHistoryDailyCountResponse(
        LocalDate date,
        int writeCount
) {
}
