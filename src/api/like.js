import api from "./axios";

// 게시글 좋아요 관련 API 요청 모음

// 게시글 좋아요 정보 조회
export const getLikeInfo = async (postId) => {
    const response = await api.get(`/api/posts/${postId}/likes`);

    return response.data.data;
};

// 게시글 좋아요 등록
export const likePost = async (postId) => {
    const response = await api.post(`/api/posts/${postId}/likes`);

    return response.data.data;
};

// 게시글 좋아요 취소
export const unlikePost = async (postId) => {
    const response = await api.delete(`/api/posts/${postId}/likes`);

    return response.data.data;
};
