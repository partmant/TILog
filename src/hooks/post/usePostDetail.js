import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
    getPostDetail,
    deletePost,
} from "../../api/post";

import {
    getComments,
    createComment,
} from "../../api/comment";

import {
    getPostEditPath,
    postListPath,
} from "../../constants/post";

// 게시글 상세 페이지 관련 로직 관리 Hook

export function usePostDetail() {
    // =========================
    // 라우팅 관련
    // =========================

    // URL 게시글 ID 조회
    const { postId } = useParams();

    // 페이지 이동 객체
    const navigate = useNavigate();

    // =========================
    // 상태 관리
    // =========================

    // 게시글 상태
    const [post, setPost] = useState(null);

    // 댓글 열기/닫기 상태
    const [showComments, setShowComments] = useState(false);

    // 댓글 목록 상태
    const [comments, setComments] = useState([]);

    // 댓글 입력값 상태
    const [commentContent, setCommentContent] = useState("");

    // =========================
    // 데이터 조회
    // =========================

    // 게시글 상세 조회
    useEffect(() => {
        const fetchPostDetail = async () => {
            const data = await getPostDetail(postId);
            setPost(data);
        };

        fetchPostDetail();
    }, [postId]);

    // 게시글 댓글 목록 조회
    useEffect(() => {
        const fetchComments = async () => {
            const data = await getComments(postId);
            setComments(data);
        };

        fetchComments();
    }, [postId]);

    // =========================
    // 이벤트 처리
    // =========================

    // 게시글 수정 페이지 이동
    const handleEdit = () => {
        navigate(getPostEditPath(postId));
    };

    // 게시글 목록 페이지 이동
    const handleMoveList = () => {
        navigate(postListPath);
    };

    // 댓글 열기/닫기 처리
    const handleToggleComments = () => {
        setShowComments((prev) => !prev);
    };

    // 댓글 입력값 변경 처리
    const handleCommentChange = (e) => {
        setCommentContent(e.target.value);
    };

    // =========================
    // API 요청
    // =========================

    // 게시글 삭제 요청
    const handleDelete = async () => {
        const confirmed = window.confirm("게시글을 삭제하시겠습니까?");

        if (!confirmed) return;

        try {
            await deletePost(postId);

            alert("게시글이 삭제되었습니다.");

            navigate(postListPath);
        } catch (error) {
            console.error(error);

            alert("게시글 삭제 실패");
        }
    };

    // 댓글 작성 요청
    const handleCreateComment = async () => {
        if (!commentContent.trim()) {
            alert("댓글 내용을 입력해주세요.");
            return;
        }

        try {
            await createComment(postId, commentContent);

            // 댓글 작성 후 목록 재조회
            const data = await getComments(postId);

            setComments(data);
            setCommentContent("");
            setShowComments(true);
        } catch (error) {
            console.error(error);

            alert("댓글 작성 실패");
        }
    };

    return {
        post,
        comments,
        commentContent,
        showComments,
        handleEdit,
        handleDelete,
        handleMoveList,
        handleCommentChange,
        handleCreateComment,
        handleToggleComments,
    };
}