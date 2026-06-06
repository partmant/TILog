import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { getPostDetail, deletePost, generatePostSummary, } from "../../api/post";

import { getComments, createComment, updateComment, deleteComment, getReplies, } from "../../api/comment";

import { getLikeInfo, likePost, unlikePost, } from "../../api/like";

import { follow, unfollow, isFollowing, } from "../../api/follow";

import { getPostEditPath, postListPath, } from "../../constants/post";
import { isLoggedIn } from "../../utils/authUtils";

// 게시글 상세 페이지 관련 로직 관리 Hook

const POST_DETAIL_LOGIN_MESSAGE = "상세 조회하려면 로그인이 필요합니다.";

// 게시글 요약 중복 요청 방지용 임시 캐시
const postSummaryCache = new Map();
const pendingPostSummaryRequests = new Map();

export function usePostDetail() {
    // =========================
    // 라우팅 관련
    // =========================

    // URL 게시글 ID 조회
    const { postId } = useParams();

    // 페이지 이동 객체
    const navigate = useNavigate();

    const loggedIn = isLoggedIn();
    const redirectedToLoginRef = useRef(false);

    const redirectToLogin = useCallback(() => {
        if (redirectedToLoginRef.current) return;

        redirectedToLoginRef.current = true;
        alert(POST_DETAIL_LOGIN_MESSAGE);
        navigate("/login", { replace: true });
    }, [navigate]);

    // =========================
    // 상태 관리
    // =========================

    // 게시글 상태
    const [post, setPost] = useState(null);

    // 게시글 핵심 요약 상태
    const [postSummary, setPostSummary] = useState("");
    const [postSummaryLoading, setPostSummaryLoading] = useState(false);
    const [postSummaryError, setPostSummaryError] = useState("");

    // 댓글 열기/닫기 상태
    const [showComments, setShowComments] = useState(false);

    // 댓글 목록 상태
    const [comments, setComments] = useState([]);

    // 댓글 입력값 상태
    const [commentContent, setCommentContent] = useState("");

    // 대댓글 목록 상태
    const [repliesMap, setRepliesMap] = useState({});

    // 대댓글 작성 대상 댓글 ID
    const [replyTargetId, setReplyTargetId] = useState(null);

    // 대댓글 입력값 상태
    const [replyContent, setReplyContent] = useState("");

    // 댓글 현재 페이지 상태
    const [commentPage, setCommentPage] = useState(0);

    // 현재 페이지 댓글 목록 상태
    const [pagedComments, setPagedComments] = useState([]);

    // 댓글 페이지 정보 상태
    const [commentPageInfo, setCommentPageInfo] = useState({
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true,
    });

    // 댓글 작성 폼 열기/닫기 상태
    const [showCommentForm, setShowCommentForm] = useState(false);

    // 좋아요 정보 상태
    const [likeInfo, setLikeInfo] = useState({
        likeCount: 0,
        liked: false,
    });

    // 팔로우 상태
    const [followInfo, setFollowInfo] = useState({
        following: false,
    });

    // 댓글 수정 대상 ID
    const [editCommentId, setEditCommentId] = useState(null);

    // 댓글 수정 입력값
    const [editCommentContent, setEditCommentContent] = useState("");

    // =========================
    // 데이터 조회
    // =========================

    // 게시글 상세 조회
    useEffect(() => {
        if (!loggedIn) {
            redirectToLogin();
        }
    }, [loggedIn, redirectToLogin]);

    useEffect(() => {
        if (!loggedIn) return;

        const fetchPostDetail = async () => {
            try {
                const data = await getPostDetail(postId);
                setPost(data);
            } catch (error) {
                if (error.response?.status === 401) {
                    redirectToLogin();
                    return;
                }

                throw error;
            }
        };

        fetchPostDetail();
    }, [postId, loggedIn, redirectToLogin]);

    // 게시글 핵심 요약 조회
    useEffect(() => {
        if (!loggedIn) return;

        let ignore = false;
        const cachedSummary = postSummaryCache.get(postId);

        if (cachedSummary !== undefined) {
            setPostSummary(cachedSummary);
            setPostSummaryError("");
            setPostSummaryLoading(false);
            return;
        }

        const fetchPostSummary = async () => {
            setPostSummary("");
            setPostSummaryError("");
            setPostSummaryLoading(true);

            try {
                // 같은 게시글 요약 요청이 진행 중이면 기존 요청을 재사용
                const request = pendingPostSummaryRequests.get(postId) || generatePostSummary(postId);
                pendingPostSummaryRequests.set(postId, request);

                const data = await request;
                const summary = data?.summary || "";
                postSummaryCache.set(postId, summary);

                if (!ignore) {
                    setPostSummary(summary);
                }
            } catch (error) {
                console.error(error);
                if (!ignore) {
                    setPostSummaryError(
                        error.response?.data?.message || "핵심 요약을 불러오지 못했습니다."
                    );
                }
            } finally {
                pendingPostSummaryRequests.delete(postId);
                if (!ignore) {
                    setPostSummaryLoading(false);
                }
            }
        };

        fetchPostSummary();

        return () => {
            ignore = true;
        };
    }, [postId, loggedIn]);

    // 게시글 댓글 및 대댓글 목록 재조회
    const refreshCommentsWithReplies = async (targetPage = commentPage) => {
        const commentData = await getComments(postId);

        const pageSize = 5;

        const currentComments = commentData.slice(
            targetPage * pageSize,
            targetPage * pageSize + pageSize
        );

        setComments(commentData);
        setPagedComments(currentComments);

        setCommentPageInfo({
            totalPages: Math.ceil(commentData.length / pageSize),
            totalElements: commentData.length,
            first: targetPage === 0,
            last: targetPage >= Math.ceil(commentData.length / pageSize) - 1,
        });

        const repliesEntries = await Promise.all(
            currentComments.map(async (comment) => {
                const replies = await getReplies(comment.commentId);
                return [comment.commentId, replies];
            })
        );

        setRepliesMap(Object.fromEntries(repliesEntries));
    };

    // 게시글 댓글 및 대댓글 목록 조회
    useEffect(() => {
        if (!loggedIn) return;

        refreshCommentsWithReplies();
    }, [postId, commentPage, loggedIn]);

    // 게시글 좋아요 정보 조회
    useEffect(() => {
        if (!loggedIn) return;

        const fetchLikeInfo = async () => {
            const data = await getLikeInfo(postId);
            setLikeInfo(data);
        };

        fetchLikeInfo();
    }, [postId, loggedIn]);

    // 팔로우 여부 조회 (다른 사람 게시글일 때만)
    useEffect(() => {
        const fetchFollowInfo = async () => {
            if (!post || post.owner) return;
            try {
                const following = await isFollowing(post.memberId);
                setFollowInfo({ following });
            } catch {
                setFollowInfo({ following: false });
            }
        };

        fetchFollowInfo();
    }, [post]);

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

    // 대댓글 입력값 변경 처리
    const handleReplyChange = (e) => {
        setReplyContent(e.target.value);
    };

    // 대댓글 작성창 열기
    const handleOpenReplyForm = (commentId) => {
        setReplyTargetId(commentId);
        setReplyContent("");
    };

    // 대댓글 작성창 닫기
    const handleCloseReplyForm = () => {
        setReplyTargetId(null);
        setReplyContent("");
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

    // 게시글 좋아요 토글 요청
    const handleToggleLike = async () => {
        try {
            const data = likeInfo.liked
                ? await unlikePost(postId)
                : await likePost(postId);

            setLikeInfo(data);
        } catch (error) {
            console.error(error);

            alert("좋아요 처리 실패");
        }
    };

    // 팔로우 토글 요청
    const handleToggleFollow = async () => {
        try {
            if (followInfo.following) {
                await unfollow(post.memberId);
                setFollowInfo({ following: false });
            } else {
                await follow(post.memberId);
                setFollowInfo({ following: true });
            }
        } catch (error) {
            console.error(error);
            alert("팔로우 처리 실패");
        }
    };

    // 댓글 수정창 열기
    const handleOpenEditComment = (commentId, currentContent) => {
        setEditCommentId(commentId);
        setEditCommentContent(currentContent);
    };

    // 댓글 수정창 닫기
    const handleCloseEditComment = () => {
        setEditCommentId(null);
        setEditCommentContent("");
    };

    // 댓글 수정 입력값 변경
    const handleEditCommentChange = (e) => {
        setEditCommentContent(e.target.value);
    };

    // 댓글 수정 요청
    const handleUpdateComment = async (commentId) => {
        if (!editCommentContent.trim()) {
            alert("댓글 내용을 입력해주세요.");
            return;
        }
        try {
            await updateComment(commentId, editCommentContent);
            await refreshCommentsWithReplies(commentPage);
            handleCloseEditComment();
        } catch (error) {
            console.error(error);
            alert("댓글 수정 실패");
        }
    };

    // 댓글 삭제 요청
    const handleDeleteComment = async (commentId) => {
        const confirmed = window.confirm("댓글을 삭제하시겠습니까?");
        if (!confirmed) return;
        try {
            await deleteComment(commentId);
            await refreshCommentsWithReplies(commentPage);
        } catch (error) {
            console.error(error);
            alert("댓글 삭제 실패");
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

            // 댓글 작성 후 현재 페이지 기준으로 재조회
            await refreshCommentsWithReplies(commentPage);

            setCommentContent("");
            setShowCommentForm(false);
            setShowComments(true);
        } catch (error) {
            console.error(error);

            alert("댓글 작성 실패");
        }
    };

    // 대댓글 작성 요청
    const handleCreateReply = async (parentCommentId) => {
        if (!replyContent.trim()) {
            alert("대댓글 내용을 입력해주세요.");
            return;
        }

        try {
            await createComment(postId, replyContent, parentCommentId);

            await refreshCommentsWithReplies(commentPage);

            setReplyTargetId(null);
            setReplyContent("");
            setShowComments(true);
        } catch (error) {
            console.error(error);

            alert("대댓글 작성 실패");
        }
    };

    // 댓글 작성창 열기
    const handleOpenCommentForm = () => {
        setShowCommentForm(true);
    };

    // 댓글 작성창 닫기
    const handleCloseCommentForm = () => {
        setShowCommentForm(false);
        setCommentContent("");
    };

    return {
        post,
        postSummary,
        postSummaryLoading,
        postSummaryError,
        comments,
        repliesMap,
        likeInfo,
        followInfo,
        commentContent,
        commentPage,
        commentPageInfo,
        setCommentPage,
        replyTargetId,
        replyContent,
        showComments,
        showCommentForm,
        editCommentId,
        editCommentContent,
        handleEdit,
        handleDelete,
        handleToggleLike,
        handleToggleFollow,
        handleMoveList,
        handleCommentChange,
        handleReplyChange,
        handleOpenReplyForm,
        handleCloseReplyForm,
        handleCreateComment,
        handleCreateReply,
        handleToggleComments,
        handleOpenCommentForm,
        handleCloseCommentForm,
        handleOpenEditComment,
        handleCloseEditComment,
        handleEditCommentChange,
        handleUpdateComment,
        handleDeleteComment,
        pagedComments,
    };
}
