import api from "./axios";

// 알림 관련 API 요청 모음

// 내 알림 목록 조회
export const getNotifications = async (page = 0, size = 20) => {
    const response = await api.get(`/api/notifications`, {
        params: { page, size },
    });
    return response.data.data;
};

// 안 읽은 알림 수 조회
export const getUnreadCount = async () => {
    const response = await api.get(`/api/notifications/unread-count`);
    return response.data.data;
};

// 알림 단건 읽음 처리
export const markAsRead = async (notificationId) => {
    const response = await api.patch(`/api/notifications/${notificationId}/read`);
    return response.data.data;
};

// 전체 읽음 처리
export const markAllAsRead = async () => {
    const response = await api.patch(`/api/notifications/read-all`);
    return response.data.data;
};

// 알림 단건 삭제
export const deleteNotification = async (notificationId) => {
    const response = await api.delete(`/api/notifications/${notificationId}`);
    return response.data.data;
};

// 알림 전체 삭제
export const deleteAllNotifications = async () => {
    const response = await api.delete(`/api/notifications`);
    return response.data.data;
};