package com.tilog.dto.history;

import java.time.LocalDate;

public record WriteHistoryHeatmapItemResponse(
        LocalDate date,
        int writeCount,
        int level
) {
}
