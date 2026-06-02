import api from "./axios";

// 게시글 목록 조회 API
export const getPostList = async () => {
    const response = await api.get("/api/posts");
    return response.data;
};

// 게시글 상세 조회 API
export const getPostDetail = async (postId, increaseViewCount = true) => {
    const response = await api.get(`/api/posts/${postId}`, {
        params: {
            increaseViewCount,
        },
    });
    return response.data;
};

// 게시글 검색 API
export const searchPosts = async ({ keyword, tagName }) => {
    const response = await api.get("/api/tils", {
        params: {
            keyword,
            tagName,
        },
    });

    return response.data.content.map((post) => ({
        postId: post.postId,
        title: post.title,
        nickname: post.authorNickname,
        difficulty: post.difficulty,
        tagNames: post.tags,
        createdAt: post.createdAt,
        likeCount: post.likeCount,
        commentCount: post.commentCount,
        viewCount: post.viewCount,
        studyTime: post.studyTime,
    }));
};

// 게시글 작성 API
export const createPost = async (postData) => {
    const response = await api.post(`/api/posts`, postData);
    return response.data;
}

// 게시글 수정 API
export const updatePost = async (postId, postData) => {
    const response = await api.put(`/api/posts/${postId}`, postData);
    return response.data;
}

// 게시글 삭제 API
export const deletePost = async (postId) => {
    const response = await api.delete(`/api/posts/${postId}`);
    return response.data;
}

// 게시글 이미지 업로드 API
export const uploadPostImage = async (formData) => {
    const response = await api.post(
        "/api/posts/images",
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );
    return response.data;
};
