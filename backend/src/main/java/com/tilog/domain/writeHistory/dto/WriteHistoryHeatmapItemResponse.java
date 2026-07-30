package com.tilog.domain.writeHistory.dto;

import java.time.LocalDate;

public record WriteHistoryHeatmapItemResponse(
        LocalDate date,
        int writeCount,
        int level
) {
}
