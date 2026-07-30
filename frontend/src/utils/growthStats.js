import {
    getCountFromHeatmapItem,
    normalizeHeatmapItems,
    normalizeProgressRate,
} from "./mypageUtils";

// 성장 요약 기본값
export const DEFAULT_GROWTH_SUMMARY = {
    currentStreak: 0,
    monthWriteCount: 0,
    monthActiveDays: 0,
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

// 이번 달 히트맵 응답에서 실제 작성한 날짜 수 추출
export const getMonthActiveDays = (heatmapResponse) => {
    if (!heatmapResponse) {
        return 0;
    }

    if (Number.isFinite(heatmapResponse.activeDays)) {
        return heatmapResponse.activeDays;
    }

    return normalizeHeatmapItems(heatmapResponse)
        .filter((item) => getCountFromHeatmapItem(item) > 0)
        .length;
};

// 월간 TIL 챌린지 진행률 계산
export const getChallengeProgressRate = (activeDays, goal = getCurrentMonthDayCount()) => {
    if (!goal) {
        return 0;
    }

    return normalizeProgressRate((activeDays / goal) * 100);
};

// 현재 달의 전체 일수 계산
export const getCurrentMonthDayCount = () => {
    const today = new Date();

    return new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();
};

// 현재 월 기준 챌린지 제목 생성
export const getMonthlyTilChallengeTitle = () => {
    return `${new Date().getMonth() + 1}월 TIL 챌린지`;
};
