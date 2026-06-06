import api from "./axios";

// 게시글 관련 API 요청 모음

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
const mapPostSummary = (post) => ({
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
});

// 게시글 검색 및 페이징 조회 API
export const searchPostPage = async ({
    keyword,
    tagName,
    nickname,
    from,
    to,
    difficulty,
    sort = "LATEST",
    page = 0,
    size = 10,
} = {}) => {
    const response = await api.get("/api/tils", {
        params: {
            keyword,
            tagName,
            nickname,
            from,
            to,
            difficulty,
            sort,
            page,
            size,
        },
    });

    const pageData = response.data;

    return {
        posts: pageData.content.map(mapPostSummary),
        page: pageData.number,
        size: pageData.size,
        totalPages: pageData.totalPages,
        totalElements: pageData.totalElements,
        first: pageData.first,
        last: pageData.last,
    };
};

// 게시글 검색 목록 조회 API
export const searchPosts = async (params = {}) => {
    const pageData = await searchPostPage(params);
    return pageData.posts;
};

// 인기 태그 조회 API
export const getPopularTags = async ({ limit = 10 } = {}) => {
    const response = await api.get("/api/tags/popular", {
        params: {
            limit,
        },
    });
    const tags = response.data.data ?? response.data;

    return tags.map((tag) => ({
        tagName: tag.tagName ?? tag.name,
        count: tag.count ?? tag.postCount ?? 0,
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


 // 게시글 또는 댓글 신고하기 API
export const submitReport = async (reportData) => {
    const response = await api.post('/api/reports', reportData);
    return response.data;
};
