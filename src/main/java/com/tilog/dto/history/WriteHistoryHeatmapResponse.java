package com.tilog.dto.history;

import java.time.LocalDate;
import java.util.List;

public record WriteHistoryHeatmapResponse(
        LocalDate startDate,
        LocalDate endDate,
        int totalWriteCount,
        int activeDays,
        int maxWriteCount,
        List<WriteHistoryHeatmapItemResponse> items
) {
}
