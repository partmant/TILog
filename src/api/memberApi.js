import { request } from './apiClient';

const PROFILE_CACHE_KEY = 'tilog:member:profile';
const PROFILE_CACHE_TTL = 1000 * 60 * 5; // 5분

const safeParseCache = () => {
    try {
        const cached = localStorage.getItem(PROFILE_CACHE_KEY);
        if (!cached) return null;
        const parsed = JSON.parse(cached);
        if (Date.now() - parsed.cachedAt < PROFILE_CACHE_TTL) return parsed.data;
        localStorage.removeItem(PROFILE_CACHE_KEY);
        return null;
    } catch {
        return null;
    }
};

const saveProfileCache = (data) => {
    localStorage.setItem(PROFILE_CACHE_KEY, JSON.stringify({
        cachedAt: Date.now(),
        data,
    }));
};

export const clearProfileCache = () => {
    localStorage.removeItem(PROFILE_CACHE_KEY);
};

// 내 정보 조회 (profileImageUrl 포함)
export const getMyProfile = async ({ useCache = true } = {}) => {
    if (useCache) {
        const cached = safeParseCache();
        if (cached) return cached;
    }
    const data = await request('/api/members/me', { method: 'GET' });
    saveProfileCache(data);
    return data;
};
//프로필 정보 업데이트
export const updateProfile = async ({ nickname, currentStatus, targetJob }) => {
    const data = await request('/api/members/me', {
        method: 'PATCH',
        body: JSON.stringify({ nickname, currentStatus, targetJob }),
    });
    clearProfileCache(data);
    return data;
};

// 프로필 이미지 업로드
export const updateProfileImage = async (file) => {
    const formData = new FormData();
    formData.append('image', file);

    const data = await request('/api/members/me/profile-image', {
        method: 'PATCH',
        body: formData,
    });


    // 캐시 갱신
    saveProfileCache(data);
    return data;
};
