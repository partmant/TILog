import api from './axios';

// 1. 내 알림 목록 조회 (최근 20개)
export const fetchMyNotifications = async (page = 0, size = 20) => {
    const response = await api.get(`/api/notifications?page=${page}&size=${size}`);
    return response.data;
};

// 2. 알림 단건 읽음 처리
export const markNotificationAsRead = async (notificationId) => {
    const response = await api.patch(`/api/notifications/${notificationId}/read`);
    return response.data;
};

// 3. 알림 전체 읽음 처리
export const markAllNotificationsAsRead = async () => {
    const response = await api.patch(`/api/notifications/read-all`);
    return response.data;
};