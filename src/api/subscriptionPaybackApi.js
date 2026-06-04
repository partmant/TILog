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

    return unwrapApiResponse(response);
};

export const cancelSubscription = async () => {
    const response = await fetch(`${API_BASE_URL}/api/subscriptions`, {
        method: 'DELETE',
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
