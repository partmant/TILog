import { request } from './apiClient';

const TEMP_MEMBER_ID = 1;

const TEN_MINUTES = 1000 * 60 * 10;
const ONE_DAY = 1000 * 60 * 60 * 24;

const getTodayString = () => {
    return new Date().toISOString().slice(0, 10);
};

const isTodayIncluded = (endDate) => {
    return endDate >= getTodayString();
};

const getHeatmapCacheTtl = (endDate) => {
    return isTodayIncluded(endDate) ? TEN_MINUTES : ONE_DAY;
};

const getStreakCacheTtl = () => {
    return TEN_MINUTES;
};

const safeParseCache = (cacheKey) => {
    const cached = localStorage.getItem(cacheKey);

    if (!cached) {
        return null;
    }

    try {
        return JSON.parse(cached);
    } catch (error) {
        localStorage.removeItem(cacheKey);
        return null;
    }
};

const isCacheFresh = ({ cachedAt, ttl }) => {
    return Date.now() - cachedAt < ttl;
};

const saveCache = ({ cacheKey, data }) => {
    localStorage.setItem(
        cacheKey,
        JSON.stringify({
            cachedAt: Date.now(),
            data,
        })
    );
};

export const getHeatmapCacheKey = ({ memberId, startDate, endDate }) => {
    return `tilog:heatmap:${memberId}:${startDate}:${endDate}`;
};

export const getStreakCacheKey = (memberId = TEMP_MEMBER_ID) => {
    return `tilog:streak:${memberId}`;
};

export const getCachedHeatmap = ({ memberId, startDate, endDate }) => {
    const cacheKey = getHeatmapCacheKey({
        memberId,
        startDate,
        endDate,
    });

    const parsedCache = safeParseCache(cacheKey);

    if (!parsedCache) {
        return null;
    }

    const ttl = getHeatmapCacheTtl(endDate);

    if (!isCacheFresh({ cachedAt: parsedCache.cachedAt, ttl })) {
        localStorage.removeItem(cacheKey);
        return null;
    }

    return parsedCache.data;
};

export const saveHeatmapCache = ({ memberId, startDate, endDate, data }) => {
    const cacheKey = getHeatmapCacheKey({
        memberId,
        startDate,
        endDate,
    });

    saveCache({
        cacheKey,
        data,
    });
};

export const getCachedStreak = (memberId = TEMP_MEMBER_ID) => {
    const cacheKey = getStreakCacheKey(memberId);
    const parsedCache = safeParseCache(cacheKey);

    if (!parsedCache) {
        return null;
    }

    if (!isCacheFresh({ cachedAt: parsedCache.cachedAt, ttl: getStreakCacheTtl() })) {
        localStorage.removeItem(cacheKey);
        return null;
    }

    return parsedCache.data;
};

export const saveStreakCache = ({ memberId = TEMP_MEMBER_ID, data }) => {
    saveCache({
        cacheKey: getStreakCacheKey(memberId),
        data,
    });
};

export const getMyStreak = async ({
                                      memberId = TEMP_MEMBER_ID,
                                      useCache = true,
                                  } = {}) => {
    if (useCache) {
        const cachedData = getCachedStreak(memberId);

        if (cachedData) {
            return cachedData;
        }
    }

    const data = await request('/api/write-histories/streak', {
        method: 'GET',
        headers: {
            'X-MEMBER-ID': String(memberId),
        },
    });

    if (useCache) {
        saveStreakCache({
            memberId,
            data,
        });
    }

    return data;
};

export const refreshMyStreak = async (memberId = TEMP_MEMBER_ID) => {
    const data = await request('/api/write-histories/streak', {
        method: 'GET',
        headers: {
            'X-MEMBER-ID': String(memberId),
        },
    });

    saveStreakCache({
        memberId,
        data,
    });

    return data;
};

export const getMyHeatmap = async ({
                                       startDate,
                                       endDate,
                                       memberId = TEMP_MEMBER_ID,
                                       useCache = true,
                                   }) => {
    if (useCache) {
        const cachedData = getCachedHeatmap({
            memberId,
            startDate,
            endDate,
        });

        if (cachedData) {
            return cachedData;
        }
    }

    const params = new URLSearchParams({
        startDate,
        endDate,
    });

    const data = await request(`/api/write-histories/heatmap?${params}`, {
        method: 'GET',
        headers: {
            'X-MEMBER-ID': String(memberId),
        },
    });

    if (useCache) {
        saveHeatmapCache({
            memberId,
            startDate,
            endDate,
            data,
        });
    }

    return data;
};

export const refreshMyHeatmap = async ({
                                           startDate,
                                           endDate,
                                           memberId = TEMP_MEMBER_ID,
                                       }) => {
    const params = new URLSearchParams({
        startDate,
        endDate,
    });

    const data = await request(`/api/write-histories/heatmap?${params}`, {
        method: 'GET',
        headers: {
            'X-MEMBER-ID': String(memberId),
        },
    });

    saveHeatmapCache({
        memberId,
        startDate,
        endDate,
        data,
    });

    return data;
};

export const clearMyHeatmapCache = (memberId = TEMP_MEMBER_ID) => {
    Object.keys(localStorage)
        .filter((key) => key.startsWith(`tilog:heatmap:${memberId}:`))
        .forEach((key) => localStorage.removeItem(key));
};

export const clearMyStreakCache = (memberId = TEMP_MEMBER_ID) => {
    localStorage.removeItem(getStreakCacheKey(memberId));
};

export const clearMyPageCache = (memberId = TEMP_MEMBER_ID) => {
    clearMyHeatmapCache(memberId);
    clearMyStreakCache(memberId);
};
