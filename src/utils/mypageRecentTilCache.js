import { normalizeTilList } from './mypageUtils';

const RECENT_TIL_CACHE_TTL = 1000 * 60 * 5;

const getRecentTilCacheKey = (memberId) => {
    return `mypage:recent-tils:member:${memberId}:page:0:size:4`;
};

export const getCachedMyRecentTils = (memberId) => {
    if (!memberId) {
        return null;
    }

    const cached = localStorage.getItem(getRecentTilCacheKey(memberId));

    if (!cached) {
        return null;
    }

    try {
        const parsed = JSON.parse(cached);

        if (Date.now() - parsed.cachedAt > RECENT_TIL_CACHE_TTL) {
            localStorage.removeItem(getRecentTilCacheKey(memberId));
            return null;
        }

        return parsed.data;
    } catch {
        localStorage.removeItem(getRecentTilCacheKey(memberId));
        return null;
    }
};

export const setCachedMyRecentTils = (memberId, data) => {
    if (!memberId) {
        return;
    }

    localStorage.setItem(
        getRecentTilCacheKey(memberId),
        JSON.stringify({
            cachedAt: Date.now(),
            data,
        })
    );
};

export const getInitialRecentTils = (memberId) => {
    return normalizeTilList(getCachedMyRecentTils(memberId));
};
