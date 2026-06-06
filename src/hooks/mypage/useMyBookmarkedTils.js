import { useEffect, useState } from "react";
import { getMyBookmarkedPosts } from "../../api/bookmark";

export function useMyBookmarkedTils() {
    const [posts, setPosts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchBookmarks = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await getMyBookmarkedPosts({ page: 0, size: 20 });
            setPosts(data.posts);
        } catch (err) {
            console.error("[BOOKMARKED TILS ERROR]", err);
            setError("즐겨찾기 목록을 불러오는 중 오류가 발생했습니다.");
            setPosts([]);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchBookmarks();
    }, []);

    const handleBookmarkChange = (postId, isBookmarked) => {
        if (!isBookmarked) {
            // 즐겨찾기 해제 시 목록에서 제거
            setPosts((prev) => prev.filter((p) => p.postId !== postId));
        }
    };

    return { posts, isLoading, error, handleBookmarkChange };
}
