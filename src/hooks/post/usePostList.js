import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { getPostList, searchPosts, } from "../../api/post";

import { postWritePath, getPostDetailPath, } from "../../constants/post";

// 게시글 목록 페이지 관련 로직 관리 Hook

export function usePostList() {
    // 페이지 이동 객체
    const navigate = useNavigate();

    // 게시글 목록 상태
    const [posts, setPosts] = useState([]);

    // URL 검색 파라미터 조회
    const [searchParams] = useSearchParams();

    // 검색어 조회
    const keyword = searchParams.get("keyword");
    const tagName = searchParams.get("tagName");

    // 게시글 목록 조회 및 검색
    useEffect(() => {
        const fetchPosts = async () => {
            let data;

            if (keyword || tagName) {
                data = await searchPosts({ keyword, tagName });
            } else {
                data = await getPostList();
            }

            setPosts(data);
        };

        fetchPosts();
    }, [keyword, tagName]);

    // 태그 클릭 시 태그 검색
    const handleSearchTag = (tagName) => {
        navigate(`/posts?tagName=${encodeURIComponent(tagName)}`);
    };

    // 게시글 작성 페이지 이동
    const handleMoveWrite = () => {
        navigate(postWritePath);
    };

    // 게시글 상세 페이지 이동
    const handleMoveDetail = (postId) => {
        navigate(getPostDetailPath(postId));
    };

    return {
        posts,
        handleMoveWrite,
        handleMoveDetail,
        handleSearchTag,
    };
}