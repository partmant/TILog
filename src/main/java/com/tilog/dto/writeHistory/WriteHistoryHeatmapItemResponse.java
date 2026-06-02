package com.tilog.dto.writeHistory;

import java.time.LocalDate;

public record WriteHistoryHeatmapItemResponse(
        LocalDate date,
        int writeCount,
        int level
) {
}
