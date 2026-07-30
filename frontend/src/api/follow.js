import api from "./axios";

// 팔로우 관련 API 요청 모음

// 팔로우
export const follow = async (targetMemberId) => {
    const response = await api.post(`/api/follows/${targetMemberId}`);
    return response.data.data;
};

// 언팔로우
export const unfollow = async (targetMemberId) => {
    const response = await api.delete(`/api/follows/${targetMemberId}`);
    return response.data.data;
};

// 팔로우 여부 확인
export const isFollowing = async (targetMemberId) => {
    const response = await api.get(`/api/follows/${targetMemberId}`);
    return response.data.data;
};

// 팔로워 목록 조회 (나를 팔로우한 사람들)
export const getFollowers = async (page = 0, size = 20) => {
    const response = await api.get(`/api/follows/followers`, {
        params: { page, size },
    });
    return response.data.data;
};

// 팔로잉 목록 조회 (내가 팔로우한 사람들)
export const getFollowings = async (page = 0, size = 20) => {
    const response = await api.get(`/api/follows/followings`, {
        params: { page, size },
    });
    return response.data.data;
};