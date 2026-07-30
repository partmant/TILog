import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { searchMyPostPage } from "../../api/post";
import { getPostDetailPath } from "../../constants/post";

// 내 TIL 목록 페이지 관련 로직 관리 Hook
// — /api/tils/me 호출, 닉네임 검색 없음, VIEWS 정렬 지원

export function useMyTilList() {
    const defaultPageSize = 10;

    const parsePositiveNumber = (value, fallback) => {
        const parsed = Number(value);
        return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
    };

    const navigate = useNavigate();

    const [posts, setPosts] = useState([]);
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: defaultPageSize,
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true,
    });

    const [searchParams, setSearchParams] = useSearchParams();

    const keyword    = searchParams.get("keyword");
    const tagName    = searchParams.get("tagName");
    const from       = searchParams.get("from");
    const to         = searchParams.get("to");
    const selectedDifficulty = searchParams.get("difficulty") || "ALL";
    const sortType   = searchParams.get("sort") || "LATEST";
    const currentPage = parsePositiveNumber(searchParams.get("page"), 0);
    const pageSize   = parsePositiveNumber(searchParams.get("size"), defaultPageSize);

    const [searchKeyword, setSearchKeyword] = useState(keyword || "");

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                const data = await searchMyPostPage({
                    keyword,
                    tagName,
                    from,
                    to,
                    difficulty: selectedDifficulty === "ALL" ? undefined : selectedDifficulty,
                    sort: sortType,
                    page: currentPage,
                    size: pageSize,
                });

                setPosts(data.posts);
                setPageInfo({
                    page: data.page,
                    size: data.size,
                    totalPages: data.totalPages,
                    totalElements: data.totalElements,
                    first: data.first,
                    last: data.last,
                });
            } catch {
                setPosts([]);
            }
        };

        fetchPosts();
    }, [keyword, tagName, from, to, selectedDifficulty, sortType, currentPage, pageSize]);

    useEffect(() => {
        setSearchKeyword(keyword || "");
    }, [keyword]);

    const updateParams = (updates) => {
        const nextParams = new URLSearchParams(searchParams);

        Object.entries(updates).forEach(([key, value]) => {
            const isDefault =
                !value ||
                value === "ALL" ||
                value === "LATEST" ||
                (key === "page" && Number(value) === 0) ||
                (key === "size" && Number(value) === defaultPageSize);

            if (isDefault) {
                nextParams.delete(key);
                return;
            }

            nextParams.set(key, value);
        });

        setSearchParams(nextParams);
    };

    const handleSearchKeyword = () => {
        if (!searchKeyword.trim()) {
            updateParams({ keyword: null, page: 0 });
            return;
        }
        updateParams({ keyword: searchKeyword.trim(), tagName: null, page: 0 });
    };

    const handleSearchTag = (tag) => {
        updateParams({ keyword: null, tagName: tag, page: 0 });
    };

    const handleChangeDifficulty = (difficulty) => {
        updateParams({ difficulty, page: 0 });
    };

    const handleChangeSort = (sort) => {
        updateParams({ sort, page: 0 });
    };

    const handleChangePage = (page) => {
        updateParams({ page });
    };

    const handleMoveDetail = (postId) => {
        navigate(getPostDetailPath(postId));
    };

    const conditions = {
        keyword:    keyword || "",
        tagName:    tagName || "",
        from:       from || "",
        to:         to || "",
        advanced:   searchParams.get("advanced") === "true",
    };

    const setCondition = (key, value) => {
        if (key === "keyword") setSearchKeyword(value);
        updateParams({ [key]: value || null, page: 0 });
    };

    const toggleAdvanced = () => {
        updateParams({ advanced: conditions.advanced ? null : "true" });
    };

    const resetConditions = () => {
        setSearchParams(new URLSearchParams({ advanced: "true" }));
        setSearchKeyword("");
    };

    return {
        posts,
        pageInfo,
        currentPage,
        pageSize,
        searchKeyword,
        setSearchKeyword,
        handleSearchKeyword,
        handleMoveDetail,
        handleSearchTag,
        selectedDifficulty,
        setSelectedDifficulty: handleChangeDifficulty,
        sortType,
        setSortType: handleChangeSort,
        setPage: handleChangePage,
        conditions,
        setCondition,
        toggleAdvanced,
        resetConditions,
    };
}
