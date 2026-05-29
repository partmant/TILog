import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getPostList } from "../../api/post";

import {
    postWritePath,
    getPostDetailPath,
} from "../../constants/post";

// 게시글 목록 페이지 관련 로직 관리 Hook

export function usePostList() {
    // 페이지 이동 객체
    const navigate = useNavigate();

    // 게시글 목록 상태
    const [posts, setPosts] = useState([]);

    // 게시글 목록 조회
    useEffect(() => {
        const fetchPosts = async () => {
            const data = await getPostList();
            setPosts(data);
        };

        fetchPosts();
    }, []);

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
    };
}