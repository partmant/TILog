import api from "./axios";

// 피드 관련 API 요청 모음

// 팔로잉 피드 조회
export const getFollowingFeed = async (page = 0, size = 20) => {
    const response = await api.get(`/api/feed/following`, {
        params: { page, size },
    });
    return response.data.data;
};

// 특정 회원의 공개 TIL 목록 조회
export const getMemberTils = async (memberId, page = 0, size = 20) => {
    const response = await api.get(`/api/feed/members/${memberId}/tils`, {
        params: { page, size },
    });
    return response.data.data;
};
