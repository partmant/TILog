import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { searchPostPage, } from "../../api/post";

import { getPostDetailPath, } from "../../constants/post";

// 게시글 목록 페이지 관련 로직 관리 Hook

export function usePostList() {
    // 기본 페이지당 게시글 개수
    const defaultPageSize = 10;

    // 양수 숫자 변환 (유효하지 않으면 기본값 사용)
    const parsePositiveNumber = (value, fallback) => {
        const parsed = Number(value);

        return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
    };

    // 페이지 이동 객체
    const navigate = useNavigate();

    // 게시글 목록 상태
    const [posts, setPosts] = useState([]);

    // 게시글 페이지 정보 상태
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: defaultPageSize,
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true,
    });

    // URL 검색 파라미터 조회
    const [searchParams, setSearchParams] = useSearchParams();

    // 검색어 조회
    const keyword = searchParams.get("keyword");
    const tagName = searchParams.get("tagName");
    const nickname = searchParams.get("nickname");
    const from = searchParams.get("from");
    const to = searchParams.get("to");

    // 선택한 난이도 필터 조회
    const selectedDifficulty = searchParams.get("difficulty") || "ALL";

    // 선택한 백엔드 정렬 기준 조회
    const sortType = searchParams.get("sort") || "LATEST";

    // 선택한 백엔드 페이지 번호 조회
    const currentPage = parsePositiveNumber(searchParams.get("page"), 0);

    // 선택한 백엔드 페이지 크기 조회
    const pageSize = parsePositiveNumber(searchParams.get("size"), defaultPageSize);

    // 검색 input 상태
    const [searchKeyword, setSearchKeyword] = useState(keyword || "");

    // 게시글 목록 조회 및 검색
    useEffect(() => {
        const fetchPosts = async () => {
            // 백엔드 검색 API에서 필터, 정렬, 페이징을 함께 처리
            const data = await searchPostPage({
                keyword,
                tagName,
                nickname,
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
        };

        fetchPosts();
    }, [keyword, tagName, nickname, from, to, selectedDifficulty, sortType, currentPage, pageSize]);

    // URL 검색어 변경 시 input 상태 동기화
    useEffect(() => {
        setSearchKeyword(keyword || "");
    }, [keyword]);

    // 게시글 목록 URL 파라미터 갱신
    const updatePostListParams = (updates) => {
        const nextParams = new URLSearchParams(searchParams);

        Object.entries(updates).forEach(([key, value]) => {
            const isDefaultValue =
                !value ||
                value === "ALL" ||
                value === "LATEST" ||
                (key === "page" && Number(value) === 0) ||
                (key === "size" && Number(value) === defaultPageSize);

            // 기본값은 URL에서 제거
            if (isDefaultValue) {
                nextParams.delete(key);
                return;
            }

            nextParams.set(key, value);
        });

        // URL 변경으로 목록을 다시 조회
        setSearchParams(nextParams);
    };

    // 검색어 검색
    const handleSearchKeyword = () => {
        if (!searchKeyword.trim()) {
            updatePostListParams({ keyword: null, page: 0 });
            return;
        }

        updatePostListParams({
            keyword: searchKeyword.trim(),
            tagName: null,
            page: 0,
        });
    };

    // 태그 클릭 시 태그 검색
    const handleSearchTag = (tagName) => {
        updatePostListParams({
            keyword: null,
            tagName,
            page: 0,
        });
    };

    // 난이도 필터 변경
    const handleChangeDifficulty = (difficulty) => {
        updatePostListParams({ difficulty, page: 0 });
    };

    // 백엔드 정렬 기준 변경
    const handleChangeSort = (sort) => {
        updatePostListParams({ sort, page: 0 });
    };

    // 백엔드 페이지 번호 변경
    const handleChangePage = (page) => {
        updatePostListParams({ page });
    };

    // 게시글 상세 페이지 이동
    const handleMoveDetail = (postId) => {
        navigate(getPostDetailPath(postId));
    };

    // AdvancedSearchPanel / SearchBar 호환용
    const conditions = {
        keyword: keyword || '',
        nickname: nickname || '',
        tagName: tagName || '',
        difficulty: selectedDifficulty === 'ALL' ? '' : selectedDifficulty,
        from: from || '',
        to: to || '',
        advanced: searchParams.get('advanced') === 'true',
    };

    const setCondition = (key, value) => {
        if (key === 'keyword') setSearchKeyword(value);
        updatePostListParams({ [key]: value || null, page: 0 });
    };

    const toggleAdvanced = () => {
        updatePostListParams({ advanced: conditions.advanced ? null : 'true' });
    };

    const resetConditions = () => {
        setSearchParams(new URLSearchParams({ advanced: 'true' }));
        setSearchKeyword('');
    };

    // 즐겨찾기 상태 즉시 갱신 (전체 재조회 없이 해당 게시글만 업데이트)
    const handleBookmarkChange = (postId, isBookmarked) => {
        setPosts((prev) =>
            prev.map((post) =>
                post.postId === postId ? { ...post, isBookmarked } : post
            )
        );
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
        handleBookmarkChange,
    };
}
