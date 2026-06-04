import { request } from './apiClient';

const RECENT_TILS_CACHE_TTL = 1000 * 60 * 5;

const getRecentTilsCacheKey = ({ page = 0, size = 4, sort = 'LATEST' } = {}) => {
    return `tilog:recent-tils:${page}:${size}:${sort}`;
};

const safeParseCache = (cacheKey) => {
    const cached = localStorage.getItem(cacheKey);

    if (!cached) {
        return null;
    }

    try {
        return JSON.parse(cached);
    } catch {
        localStorage.removeItem(cacheKey);
        return null;
    }
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

export const getCachedRecentTils = ({ page = 0, size = 4, sort = 'LATEST' } = {}) => {
    const cacheKey = getRecentTilsCacheKey({
        page,
        size,
        sort,
    });

    const parsedCache = safeParseCache(cacheKey);

    if (!parsedCache) {
        return null;
    }

    const isFresh = Date.now() - parsedCache.cachedAt < RECENT_TILS_CACHE_TTL;

    if (!isFresh) {
        localStorage.removeItem(cacheKey);
        return null;
    }

    return parsedCache.data;
};

export const saveRecentTilsCache = ({
                                        page = 0,
                                        size = 4,
                                        sort = 'LATEST',
                                        data,
                                    }) => {
    const cacheKey = getRecentTilsCacheKey({
        page,
        size,
        sort,
    });

    saveCache({
        cacheKey,
        data,
    });
};

export const getRecentTils = async ({
                                        page = 0,
                                        size = 4,
                                        sort = 'LATEST',
                                        useCache = true,
                                    } = {}) => {
    if (useCache) {
        const cachedData = getCachedRecentTils({
            page,
            size,
            sort,
        });

        if (cachedData) {
            return cachedData;
        }
    }

    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort,
    });

    const data = await request(`/api/tils?${params}`, {
        method: 'GET',
    });

    if (useCache) {
        saveRecentTilsCache({
            page,
            size,
            sort,
            data,
        });
    }

    return data;
};

export const refreshRecentTils = async ({
                                            page = 0,
                                            size = 4,
                                            sort = 'LATEST',
                                        } = {}) => {
    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort,
    });

    const data = await request(`/api/tils?${params}`, {
        method: 'GET',
    });

    saveRecentTilsCache({
        page,
        size,
        sort,
        data,
    });

    return data;
};

export const clearRecentTilsCache = () => {
    Object.keys(localStorage)
        .filter((key) => key.startsWith('tilog:recent-tils:'))
        .forEach((key) => localStorage.removeItem(key));
};
