import api from "./axios";

// 즐겨찾기 관련 API 요청 모음

// 즐겨찾기 등록
export const addBookmark = async (postId) => {
    const response = await api.post(`/api/posts/${postId}/bookmarks`);
    return response.data.data;
};

// 즐겨찾기 해제
export const removeBookmark = async (postId) => {
    const response = await api.delete(`/api/posts/${postId}/bookmarks`);
    return response.data.data;
};

// 내가 즐겨찾기한 TIL 목록 조회
export const getMyBookmarkedPosts = async ({
    page = 0,
    size = 10,
    keyword,
    difficulty,
    sortType = "LATEST",
} = {}) => {
    const response = await api.get("/api/posts/bookmarks/me", {
        params: {
            page,
            size,
            ...(keyword ? { keyword } : {}),
            ...(difficulty && difficulty !== "ALL" ? { difficulty } : {}),
            sortType,
        },
    });

    const pageData = response.data;

    return {
        posts: (pageData.content || []).map((post) => ({
            postId: post.postId,
            title: post.title,
            nickname: post.nickname,
            difficulty: post.difficulty,
            studyTime: post.studyTime,
            viewCount: post.viewCount,
            likeCount: post.likeCount,
            commentCount: post.commentCount,
            tagNames: post.tagNames || [],
            createdAt: post.createdAt,
            isBookmarked: post.isBookmarked ?? true,
        })),
        page: pageData.number,
        size: pageData.size,
        totalPages: pageData.totalPages,
        totalElements: pageData.totalElements,
        first: pageData.first,
        last: pageData.last,
    };
};
