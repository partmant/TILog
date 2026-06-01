package com.tilog.dto.history;

import java.time.LocalDate;
import java.util.List;

public record WriteHistoryDailyCountSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<WriteHistoryDailyCountResponse> items
) { }
