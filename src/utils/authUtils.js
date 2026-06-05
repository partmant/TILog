/**
 * JWT payload를 base64 디코딩하여 파싱합니다.
 * @param {string} token
 * @returns {object|null}
 */
const parseJwt = (token) => {
    try {
        const payload = token.split('.')[1];
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch {
        return null;
    }
};

/**
 * localStorage의 accessToken을 읽어 JWT payload를 반환합니다.
 * @returns {object|null}
 */
export const getTokenPayload = () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    return parseJwt(token);
};

/**
 * 현재 로그인한 유저 정보를 반환합니다.
 * @returns {{ memberId: number|null, nickname: string, email: string, role: string }|null}
 */
export const getCurrentUser = () => {
    const payload = getTokenPayload();
    if (!payload) return null;

    // sub에 memberId(숫자 문자열), nickname/email/role 클레임 포함
    const memberId = payload.sub ? Number(payload.sub) : null;
    const nickname = payload.nickname ?? 'user';
    const email = payload.email ?? '';
    const role = payload.role ?? '';
    const createdAt = payload.createdAt ?? null;

    return { memberId, nickname, email, role, createdAt };
};

/**
 * 현재 로그인한 유저의 memberId를 반환합니다.
 * @returns {number|null}
 */
export const getMemberId = () => {
    return getCurrentUser()?.memberId ?? null;
};

export const clearAuthStorage = () => {
    localStorage.removeItem('accessToken');

    Object.keys(localStorage)
        .filter((key) => key.startsWith('tilog:'))
        .forEach((key) => localStorage.removeItem(key));
};

export const hasRole = (targetRole) => {
    const role = getCurrentUser()?.role ?? '';

    return role === targetRole || role === `ROLE_${targetRole}`;
};

export const isPremiumUser = () => {
    return hasRole('PREMIUM');
};

/**
 * 로그인 여부를 확인합니다. (토큰 존재 + 만료 여부)
 * @returns {boolean}
 */
export const isLoggedIn = () => {
    const payload = getTokenPayload();
    if (!payload) return false;

    // 만료 시간 체크
    if (payload.exp && Date.now() / 1000 > payload.exp) {
        localStorage.removeItem('accessToken');
        return false;
    }

    return true;
};

/**
 * 로그아웃 처리 (토큰 제거)
 */
export const logout = () => {
    clearAuthStorage();
};
