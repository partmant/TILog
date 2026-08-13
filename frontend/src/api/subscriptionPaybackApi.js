const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

const getAuthHeaders = () => {
    const token = localStorage.getItem('accessToken');

    if (!token) {
        return {
            'Content-Type': 'application/json',
        };
    }

    return {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
    };
};

const unwrapApiResponse = async (response) => {
    const result = await response.json().catch(() => null);

    if (!response.ok) {
        const message = result?.message ?? '요청 처리 중 오류가 발생했습니다.';
        throw new Error(message);
    }

    return result?.data ?? result;
};

export const getMySubscriptionStatus = async () => {
    const response = await fetch(`${API_BASE_URL}/api/subscriptions/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    return unwrapApiResponse(response);
};

export const subscribePremium = async () => {
    const response = await fetch(`${API_BASE_URL}/api/subscriptions`, {
        method: 'POST',
        headers: getAuthHeaders(),
    });

    const data = await unwrapApiResponse(response);

    // 구독으로 role이 PREMIUM으로 바뀌면 서버가 새 accessToken을 함께 내려준다.
    // 즉시 교체하지 않으면 재로그인 전까지 기존 토큰의 옛 role(USER)로 프리미엄
    // 전용 페이지(피드백 등) 접근이 계속 막힌다.
    if (data?.accessToken) {
        localStorage.setItem('accessToken', data.accessToken);
    }

    return data;
};

export const cancelSubscription = async () => {
    const response = await fetch(`${API_BASE_URL}/api/subscriptions`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    return unwrapApiResponse(response);
};

export const resumeSubscription = async () => {
    const response = await fetch(`${API_BASE_URL}/api/subscriptions/resume`, {
        method: 'PATCH',
        headers: getAuthHeaders(),
    });

    return unwrapApiResponse(response);
};

export const getCurrentPaybackParticipation = async () => {
    const response = await fetch(`${API_BASE_URL}/api/payback-participations/me/current`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    return unwrapApiResponse(response);
};
