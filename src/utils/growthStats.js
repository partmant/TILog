import {
    getCountFromHeatmapItem,
    normalizeHeatmapItems,
} from "./mypageUtils";

// 성장 요약 기본값
export const DEFAULT_GROWTH_SUMMARY = {
    currentStreak: 0,
    monthWriteCount: 0,
};

// 스트릭 응답에서 현재 연속 작성일 추출
export const getCurrentStreak = (streakResponse) => {
    return streakResponse?.currentStreak ?? 0;
};

// 이번 달 히트맵 응답에서 작성 개수 합산
export const getMonthWriteCount = (heatmapResponse) => {
    if (!heatmapResponse) {
        return 0;
    }

    if (Number.isFinite(heatmapResponse.totalWriteCount)) {
        return heatmapResponse.totalWriteCount;
    }

    return normalizeHeatmapItems(heatmapResponse)
        .reduce((sum, item) => sum + getCountFromHeatmapItem(item), 0);
};
