import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

// Markdown 렌더링 라이브러리
import ReactMarkdown from "react-markdown";

// GitHub 스타일 Markdown 확장
import remarkGfm from "remark-gfm";

// 코드 블럭 문법 하이라이팅
import rehypeHighlight from "rehype-highlight";

// 코드 하이라이트 스타일
import "highlight.js/styles/github-dark.css";

import {
    getPostDetail,
    deletePost,
} from "../../api/post";

// 게시글 상세 페이지
function PostDetailPage() {
    // URL 게시글 ID 조회
    const { postId } = useParams();

    // 페이지 이동 객체
    const navigate = useNavigate();

    // 게시글 상태
    const [post, setPost] = useState(null);

    // 댓글 열기/닫기 상태
    const [showComments, setShowComments] = useState(false);

    // 임시 태그 데이터
    const tags = ["Java", "Spring Security", "JWT", "Markdown"];

    // 게시글 상세 조회
    useEffect(() => {
        const fetchPostDetail = async () => {
            const data = await getPostDetail(postId);
            setPost(data);
        };

        fetchPostDetail();
    }, [postId]);

    // 게시글 수정 이동
    const handleEdit = () => {
        navigate(`/posts/${postId}/edit`);
    };

    // 게시글 삭제
    const handleDelete = async () => {
        const confirmed = window.confirm(
            "게시글을 삭제하시겠습니까?"
        );

        if (!confirmed) return;

        try {
            await deletePost(postId);

            alert("게시글이 삭제되었습니다.");

            navigate("/posts");
        } catch (error) {
            console.error(error);

            alert("게시글 삭제 실패");
        }
    };

    // 게시글 로딩 상태
    if (!post) {
        return (
            <div className="p-10">
                로딩중...
            </div>
        );
    }

    return (
        <main className="rounded-3xl bg-white/90 p-8 shadow-sm">

            {/* 게시글 제목 */}
            <h2 className="text-3xl font-bold text-slate-900">
                {post.title}
            </h2>

            {/* 게시글 정보 */}
            <p className="mt-3 text-sm text-gray-500">
                {post.nickname}
                · 조회수 {post.viewCount}
                · 학습시간 {post.studyTime}분
                · 난이도 {post.difficulty}
            </p>

            {/* 태그 영역 */}
            <div className="mt-6 flex gap-3">
                {tags.map((tag) => (
                    <span
                        key={tag}
                        className="rounded-full bg-slate-100 px-5 py-2 text-sm font-bold text-slate-700"
                    >
                        {tag}
                    </span>
                ))}
            </div>

            {/* Markdown 본문 영역 */}
            <section className="mt-8 overflow-hidden rounded-2xl bg-slate-50">
                {/* Markdown 상단 헤더 */}
                <div className="flex justify-between bg-indigo-50 px-6 py-4 text-sm font-bold">
                    <span className="text-indigo-500">
                        Markdown Preview
                    </span>

                    <span className="text-gray-500">
                        긴 본문 스크롤 영역
                    </span>
                </div>

                {/* Markdown 렌더링 */}
                <div className="prose max-w-none max-h-[360px] overflow-y-auto p-6">
                    <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        rehypePlugins={[rehypeHighlight]}
                    >
                        {post.content}
                    </ReactMarkdown>
                </div>
            </section>

            {/* 게시글 기능 버튼 */}
            <div className="mt-6 flex gap-3">
                <button className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-6 py-3 font-bold text-white">
                    좋아요 12
                </button>

                <button className="rounded-xl border border-indigo-100 bg-indigo-50 px-6 py-3 font-bold text-indigo-500">
                    북마크
                </button>

                <button
                    onClick={handleEdit}
                    className="rounded-xl border border-indigo-100 bg-indigo-50 px-6 py-3 font-bold text-indigo-500"
                >
                    수정
                </button>

                <button
                    onClick={handleDelete}
                    className="rounded-xl border border-red-100 bg-red-50 px-6 py-3 font-bold text-red-500"
                >
                    삭제
                </button>

                {/* 게시글 목록 이동 */}
                <button
                    onClick={() => navigate("/posts")}
                    className="rounded-xl border px-6 py-3 font-bold"
                >
                    목록
                </button>
            </div>

            {/* 댓글 영역 */}
            <section className="mt-8 rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <h3 className="text-xl font-bold">
                            댓글 12개
                        </h3>

                        <p className="text-sm text-gray-500">
                            댓글은 본문 집중을 위해 기본적으로 접힌 상태입니다.
                        </p>
                    </div>

                    {/* 댓글 토글 버튼 */}
                    <button
                        onClick={() => setShowComments(!showComments)}
                        className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-6 py-3 font-bold text-white"
                    >
                        {showComments ? "댓글 닫기 ▲" : "댓글 보기 ▼"}
                    </button>
                </div>

                {/* 댓글 표시 */}
                {showComments && (
                    <div className="mt-6 space-y-4">

                        {/* 댓글 아이템 */}
                        <div className="rounded-xl bg-slate-50 p-4">
                            <p className="font-bold">
                                user02
                            </p>

                            <p className="mt-2 text-sm text-gray-600">
                                좋은 정리입니다. JWT 흐름 이해에 도움됐어요.
                            </p>
                        </div>

                        {/* 댓글 아이템 */}
                        <div className="rounded-xl bg-slate-50 p-4">
                            <p className="font-bold">
                                user03
                            </p>

                            <p className="mt-2 text-sm text-gray-600">
                                코드 예시도 같이 있으면 더 좋을 것 같아요.
                            </p>
                        </div>

                        {/* 댓글 입력 */}
                        <div className="flex gap-3">
                            <input
                                className="flex-1 rounded-xl border px-4 py-3 outline-none"
                                placeholder="댓글을 입력하세요"
                            />

                            <button className="rounded-xl bg-slate-900 px-6 py-3 font-bold text-white">
                                등록
                            </button>
                        </div>
                    </div>
                )}
            </section>
        </main>
    );
}

export default PostDetailPage;