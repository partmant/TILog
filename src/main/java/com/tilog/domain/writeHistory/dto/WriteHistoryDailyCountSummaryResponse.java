package com.tilog.domain.writeHistory.dto;

import java.time.LocalDate;
import java.util.List;

public record WriteHistoryDailyCountSummaryResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<WriteHistoryDailyCountResponse> items
) { }
