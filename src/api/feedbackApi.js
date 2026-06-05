// src/api/feedbackApi.js
import api from './axios';

// 1. 멘토 피드백 요청 (프리미엄 유저용)
export const requestFeedback = async (tilId, requestorId, mentorId, comment) => {
    // 🔥 백엔드의 FeedbackRequestDtoRequest 필드명과 100% 일치해야 합니다!
    const response = await api.post('/api/feedbacks', {
        tilId: tilId,
        requestorId: requestorId,
        mentorId: mentorId,
        comment: comment
    });
    return response.data;
};

// 2. 멘토 피드백 작성 (멘토용)
export const writeFeedback = async (feedbackId, mentorId, technicalScore, flowScore, designScore, comment) => {
    const response = await api.patch(`/api/feedbacks/${feedbackId}`, {
        mentorId: mentorId,
        technicalScore: technicalScore,
        flowScore: flowScore,
        designScore: designScore,
        comment: comment
    });
    return response.data;
};

// 3. 피드백 상세/목록 조회 (임시로 목록을 가져온다고 가정)
export const fetchFeedbackList = async () => {
    const response = await api.get('/api/feedbacks');
    return response.data;
};

// 4. 피드백 상세 조회 (백엔드 GET /api/feedbacks/{feedbackId} 연동)
export const getFeedbackDetail = async (feedbackId) => {
    const response = await api.get(`/api/feedbacks/${feedbackId}`);
    return response.data; // FeedbackDetailResponseDto 가 반환됩니다!
};