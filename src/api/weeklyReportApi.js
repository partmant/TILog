import { request } from './apiClient';
import { TEMP_MEMBER_ID } from '../utils/mypageUtils';

// GET /api/members/{id}/weekly-reports/latest — null if none (204)
export const getLatestWeeklyReport = (memberId = TEMP_MEMBER_ID) =>
    request(`/api/members/${memberId}/weekly-reports/latest`, { method: 'GET' });

// GET /api/members/{id}/weekly-reports?weekStart=YYYY-MM-DD — null if none (204)
export const getWeeklyReport = (memberId = TEMP_MEMBER_ID, weekStart) => {
    const qs = weekStart ? `?weekStart=${weekStart}` : '';
    return request(`/api/members/${memberId}/weekly-reports${qs}`, { method: 'GET' });
};

// POST /api/members/{id}/weekly-reports?weekStart=YYYY-MM-DD
export const generateWeeklyReport = (memberId = TEMP_MEMBER_ID, weekStart) => {
    const qs = weekStart ? `?weekStart=${weekStart}` : '';
    return request(`/api/members/${memberId}/weekly-reports${qs}`, { method: 'POST' });
};