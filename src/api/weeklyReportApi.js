import { request } from './apiClient';
import { getMemberId } from '../utils/authUtils';

// GET /api/members/{id}/weekly-reports/latest — null if none (204)
export const getLatestWeeklyReport = () => {
    const memberId = getMemberId();
    return request(`/api/members/${memberId}/weekly-reports/latest`, { method: 'GET' })};

// GET /api/members/{id}/weekly-reports?weekStart=YYYY-MM-DD — null if none (204)
export const getWeeklyReport = (weekStart) => {
    const memberId = getMemberId();
    const qs = weekStart ? `?weekStart=${weekStart}` : '';
    return request(`/api/members/${memberId}/weekly-reports${qs}`, { method: 'GET' });
};

// POST /api/members/{id}/weekly-reports?weekStart=YYYY-MM-DD
export const generateWeeklyReport = (weekStart) => {
    const memberId = getMemberId();
    const qs = weekStart ? `?weekStart=${weekStart}` : '';
    return request(`/api/members/${memberId}/weekly-reports${qs}`, { method: 'POST' });
};