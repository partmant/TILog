import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { getMyBookmarkedPosts } from "../../api/bookmark";

const DEFAULT_PAGE_SIZE = 10;

/**
 * 즐겨찾기 전체보기 페이지용 Hook
 * — 검색(keyword), 난이도(difficulty), 정렬(sortType), 페이지네이션 지원
 */
export function useBookmarkedTilList() {
    const [searchParams, setSearchParams] = useSearchParams();

    const currentPage = Math.max(Number(searchParams.get("page") || 0), 0);
    const keyword = searchParams.get("keyword") || "";
    const selectedDifficulty = searchParams.get("difficulty") || "ALL";
    const sortType = searchParams.get("sort") || "LATEST";

    const [posts, setPosts] = useState([]);
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true,
    });
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;
        setIsLoading(true);
        setError(null);

        getMyBookmarkedPosts({
            page: currentPage,
            size: DEFAULT_PAGE_SIZE,
            keyword: keyword || undefined,
            difficulty: selectedDifficulty === "ALL" ? undefined : selectedDifficulty,
            sortType,
        })
            .then((data) => {
                if (!isMounted) return;
                setPosts(data.posts);
                setPageInfo({
                    page: data.page,
                    totalPages: data.totalPages,
                    totalElements: data.totalElements,
                    first: data.first,
                    last: data.last,
                });
            })
            .catch((err) => {
                if (!isMounted) return;
                console.error("[BOOKMARKED LIST ERROR]", err);
                setError("즐겨찾기 목록을 불러오는 중 오류가 발생했습니다.");
            })
            .finally(() => {
                if (isMounted) setIsLoading(false);
            });

        return () => { isMounted = false; };
    }, [currentPage, keyword, selectedDifficulty, sortType]);

    // URL 파라미터 일괄 업데이트
    const updateParams = (updates) => {
        const nextParams = new URLSearchParams(searchParams);
        Object.entries(updates).forEach(([key, value]) => {
            const isDefault =
                !value ||
                value === "ALL" ||
                value === "LATEST" ||
                (key === "page" && Number(value) === 0);
            if (isDefault) {
                nextParams.delete(key);
            } else {
                nextParams.set(key, String(value));
            }
        });
        setSearchParams(nextParams);
    };

    const setPage = (page) => updateParams({ page });
    const setSelectedDifficulty = (difficulty) => updateParams({ difficulty, page: 0 });
    const setSortType = (sort) => updateParams({ sort, page: 0 });
    const setKeyword = (kw) => updateParams({ keyword: kw || null, page: 0 });

    // 즐겨찾기 해제 시 목록에서 즉시 제거
    const handleBookmarkChange = (postId, isBookmarked) => {
        if (!isBookmarked) {
            setPosts((prev) => prev.filter((p) => p.postId !== postId));
            setPageInfo((prev) => ({
                ...prev,
                totalElements: Math.max(prev.totalElements - 1, 0),
            }));
        }
    };

    return {
        posts,
        pageInfo,
        currentPage,
        keyword,
        selectedDifficulty,
        sortType,
        isLoading,
        error,
        setPage,
        setSelectedDifficulty,
        setSortType,
        setKeyword,
        handleBookmarkChange,
    };
}
