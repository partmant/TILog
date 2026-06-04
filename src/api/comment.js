import api from "./axios";

// 게시글 댓글 관련 API 요청 모음

// 게시글 댓글 목록 조회
export const getComments = async (postId) => {
    const response = await api.get(`/api/posts/${postId}/comments`);

    return response.data.data;
};

// 댓글 작성
export const createComment = async (postId, content, parentCommentId = null) => {
    const response = await api.post(`/api/posts/${postId}/comments`, {
        content,
        parentCommentId,
    });

    return response.data.data;
};

// 댓글 수정
export const updateComment = async (commentId, content) => {
    const response = await api.patch(`/api/comments/${commentId}`, {
        content,
    });

    return response.data.data;
};

// 댓글 삭제
export const deleteComment = async (commentId) => {
    const response = await api.delete(`/api/comments/${commentId}`);

    return response.data.data;
};

// 대댓글 목록 조회
export const getReplies = async (commentId) => {
    const response = await api.get(`/api/comments/${commentId}/replies`);

    return response.data.data;
};