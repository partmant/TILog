package com.tilog.dto.writeHistory;

import java.time.LocalDate;
import java.util.List;

public record WriteHistoryDailyCountSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<WriteHistoryDailyCountResponse> items
) { }
