import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";

import "highlight.js/styles/github-dark.css";

import { usePostDetail } from "../../hooks/post/usePostDetail";

// 게시글 상세 페이지
function PostDetailPage() {
    // 날짜 포맷
    const formatDate = (dateString) => {
        const date = new Date(dateString);

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");

        return `${year}.${month}.${day}`;
    };

    const {
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
    } = usePostDetail();

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
            <div className="mt-4 flex flex-wrap items-center justify-between gap-x-6 gap-y-2 border-y border-gray-200 py-2 text-sm text-slate-700">
                <div className="flex items-center gap-2 font-medium">
                    <span>{post.nickname}</span>
                </div>

                <div className="flex flex-wrap items-center gap-y-1 text-xs font-semibold">
                    <span className="px-2">
                        조회수&nbsp;
                        <span className="text-blue-600">{post.viewCount}회</span>
                    </span>

                    <span className="h-3 w-px bg-gray-300" />

                    <span className="px-2">
                        학습시간&nbsp;
                        <span className="text-blue-600">{post.studyTime}</span>
                        분
                    </span>

                    <span className="h-3 w-px bg-gray-300" />

                    <span className="px-2">
                        난이도&nbsp;
                        <span className="text-blue-600">{post.difficulty}</span>
                    </span>

                    <span className="px-2">
                        작성일&nbsp;
                        <span className="text-blue-600">{formatDate(post.createdAt)}</span>
                    </span>

                </div>
            </div>

            {/* 태그 */}
            {post.tagNames?.length > 0 && (
                <div className="mt-2 flex flex-wrap gap-2">
                    {post.tagNames.map((tag) => (
                        <span
                            key={tag}
                            className="rounded-full bg-purple-50 px-2.5 py-1 text-xs font-semibold text-purple-600"
                        >
                            #{tag}
                        </span>
                    ))}
                </div>
            )}

            {/* Markdown 본문 영역 */}
            <section className="mt-8 overflow-hidden rounded-2xl bg-slate-50">
                {/* Markdown 렌더링 */}
                <div className="prose max-w-none p-6">
                    <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        rehypePlugins={[rehypeHighlight]}
                        components={{
                            // Markdown 이미지 표시 스타일
                            img: ({ node, ...props }) => (
                                <img
                                    {...props}
                                    className="my-4 max-w-full rounded-xl object-contain"
                                />
                            ),
                        }}
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
                    onClick={handleMoveList}
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
                            댓글 {comments.length}개
                        </h3>

                        <p className="text-sm text-gray-500">
                            댓글은 본문 집중을 위해 기본적으로 접힌 상태입니다.
                        </p>
                    </div>

                    {/* 댓글 토글 버튼 */}
                    <button
                        onClick={handleToggleComments}
                        className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-6 py-3 font-bold text-white"
                    >
                        {showComments ? "댓글 닫기 ▲" : "댓글 보기 ▼"}
                    </button>
                </div>

                {/* 댓글 표시 */}
                {showComments && (
                    <div className="mt-6 space-y-4">
                        {/* 댓글 목록 */}
                        {comments.length > 0 ? (
                            comments.map((comment) => (
                                <div
                                    key={comment.commentId}
                                    className="rounded-xl bg-slate-50 p-4"
                                >
                                    <p className="font-bold">
                                        {comment.nickname}
                                    </p>

                                    <p className="mt-2 text-sm text-gray-600">
                                        {comment.content}
                                    </p>
                                </div>
                            ))
                        ) : (
                            <p className="rounded-xl bg-slate-50 p-4 text-sm text-gray-500">
                                아직 작성된 댓글이 없습니다.
                            </p>
                        )}

                        {/* 댓글 입력 */}
                        <div className="flex gap-3">
                            <input
                                value={commentContent}
                                onChange={handleCommentChange}
                                className="flex-1 rounded-xl border px-4 py-3 outline-none"
                                placeholder="댓글을 입력하세요"
                            />

                            <button
                                type="button"
                                onClick={handleCreateComment}
                                className="rounded-xl bg-slate-900 px-6 py-3 font-bold text-white"
                            >
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