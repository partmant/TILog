const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const request = async (url, options = {}) => {
    const token = localStorage.getItem('accessToken');

    const headers = {
        ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {}),
    };

    const response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers,
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'API 요청에 실패했습니다.');
    }

    if (response.status === 204) {
        return null;
    }

    const result = await response.json();

    console.log('[API RESPONSE]', url, result);

    if (result?.data !== undefined) {
        return result.data;
    }

    if (result?.result !== undefined) {
        return result.result;
    }

    return result;
};
