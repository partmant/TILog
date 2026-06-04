// src/api/adminApi.js
import api from './axios'; // 팀원들이 만들어둔 axios 인스턴스 가져오기

/**
 * 1. 회원 목록 페이징 조회
 * @param {number} page 페이지 번호 (기본값: 0)
 * @param {number} size 한 페이지당 데이터 개수 (기본값: 10)
 */
export const fetchMemberList = async (page = 0, size = 10) => {
    const response = await api.get(`/api/admin/members?page=${page}&size=${size}`);
    return response.data;
};

/**
 * 2. 특정 회원 상세 조회
 * @param {number} memberId 회원 PK
 */
export const fetchMemberDetail = async (memberId) => {
    const response = await api.get(`/api/admin/members/${memberId}`);
    return response.data;
};

/**
 * 3. 회원 권한 변경 (멘토 승격 포함)
 * @param {number} memberId 회원 PK
 * @param {string} role 변경할 권한 (예: 'MENTOR', 'PREMIUM')
 */
export const changeMemberRole = async (memberId, role) => {
    const response = await api.patch('/api/admin/members/role', {
        memberId,
        role
    });
    return response.data;
};

/**
 * 4. 게시글 강제 삭제
 * @param {number} postId 게시글 PK
 */
export const forceDeletePost = async (postId) => {
    const response = await api.delete(`/api/admin/posts/${postId}`);
    return response.data;
};

/**
 * 5. 신고 처리 및 제재
 * @param {number} reportId 신고 PK
 * @param {object} sanctionData 제재 정보 { sanctionType, reasonType, content }
 */
export const doSanction = async (reportId, sanctionData) => {
    // DTO 형식에 맞춰서 통째로 body에 실어 보냅니다 (@RequestBody 매핑용)
    const response = await api.post(`/api/admin/reports/${reportId}/sanction`, sanctionData);
    return response.data;
};

/**
 * 최근 신고 목록 4건 조회
 */
export const fetchRecentReports = async () => {
    const response = await api.get('/api/admin/reports/recent');
    return response.data; // 백엔드에서 만든 List<ReportResponseDto>가 튀어나옵니다!
};