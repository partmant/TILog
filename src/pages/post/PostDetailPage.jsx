import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";

import "highlight.js/styles/github-dark.css";

import { usePostDetail } from "../../hooks/post";
import { difficultyStyle } from "../../constants/post";

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
        pagedComments,
        repliesMap,
        likeInfo,
        commentContent,
        commentPage,
        commentPageInfo,
        setCommentPage,
        replyTargetId,
        replyContent,
        handleEdit,
        handleDelete,
        handleToggleLike,
        handleMoveList,
        handleCommentChange,
        handleReplyChange,
        handleOpenReplyForm,
        handleCloseReplyForm,
        handleCreateComment,
        handleCreateReply,
        showCommentForm,
        handleOpenCommentForm,
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
            {/* 상세 페이지 전체 레이아웃 */}
            <div className="grid grid-cols-[1fr_320px] gap-8">
                {/* 왼쪽 게시글 본문 영역 */}
                <article className="rounded-3xl border border-gray-200 bg-white p-8 shadow-sm">
                    {/* 난이도 배지 */}
                    <span
                        className={`inline-flex rounded-full px-5 py-2 text-sm font-bold ${
                            difficultyStyle[post.difficulty] || difficultyStyle.NORMAL
                        }`}
                    >
                        {post.difficulty}
                    </span>

                    {/* 게시글 제목 */}
                    <h2 className="mt-4 text-4xl font-bold text-slate-900">
                        {post.title}
                    </h2>

                    {/* 게시글 메타 정보 */}
                    <p className="mt-4 text-sm font-semibold text-gray-500">
                        {post.nickname}
                        {" · "}
                        {formatDate(post.createdAt)}
                        {" · "}
                        조회수 {post.viewCount}회
                        {" · "}
                        좋아요 {likeInfo.likeCount}개
                        {" · "}
                        댓글 {commentPageInfo.totalElements}개
                        {" · "}
                        학습시간 {post.studyTime}분
                    </p>

                    {/* 구분선 */}
                    <div className="my-8 h-px bg-gray-200" />

                    {/* Markdown 본문 영역 */}
                    <section className="min-h-[260px]">
                        <div className="prose max-w-none text-slate-700">
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

                    {/* 핵심 요약 박스 - 현재는 본문 하단 보조 UI */}
                    <section className="mt-10 rounded-2xl border border-gray-200 bg-slate-50 p-6">
                        <h3 className="font-bold text-slate-900">
                            핵심 요약
                        </h3>

                        <p className="mt-3 text-sm font-semibold leading-7 text-slate-600">
                            오늘 학습한 내용을 바탕으로 복습할 핵심 개념을 정리해보세요.
                        </p>
                    </section>

                    {/* 태그 */}
                    {post.tagNames?.length > 0 && (
                        <div className="mt-8 flex flex-wrap gap-2">
                            {post.tagNames.map((tag) => (
                                <span
                                    key={tag}
                                    className="rounded-full bg-purple-50 px-3 py-1 text-sm font-bold text-purple-600"
                                >
                                    #{tag}
                                </span>
                            ))}
                        </div>
                    )}

                    {/* 게시글 기능 버튼 */}
                    <div className="mt-10 flex flex-wrap gap-4">
                        {post.owner && (
                            <>
                                {/* 게시글 수정 */}
                                <button
                                    onClick={handleEdit}
                                    className="rounded-2xl bg-slate-900 px-8 py-3 font-bold text-white transition hover:bg-slate-700"
                                >
                                    수정하기
                                </button>

                                {/* 게시글 삭제 */}
                                <button
                                    onClick={handleDelete}
                                    className="rounded-2xl border border-red-100 bg-red-50 px-8 py-3 font-bold text-red-500 transition hover:bg-red-100"
                                >
                                    삭제하기
                                </button>
                            </>
                        )}

                        {/* 게시글 목록 이동 */}
                        <button
                            onClick={handleMoveList}
                            className="rounded-2xl bg-gray-100 px-8 py-3 font-bold text-gray-600 transition hover:bg-gray-200"
                        >
                            목록으로
                        </button>

                        {/* 게시글 좋아요 */}
                        <button
                            type="button"
                            onClick={handleToggleLike}
                            className={`rounded-2xl px-8 py-3 font-bold transition ${
                                likeInfo.liked
                                    ? "bg-pink-500 text-white hover:bg-pink-600"
                                    : "bg-pink-50 text-pink-500 hover:bg-pink-100"
                            }`}
                        >
                            좋아요 {likeInfo.likeCount}
                        </button>
                    </div>
                </article>

                {/* 오른쪽 댓글 / 보조 패널 */}
                <aside className="rounded-3xl border border-gray-200 bg-white p-6 shadow-sm">
                    {/* 댓글 헤더 */}
                    <div className="flex items-center justify-between">
                        <h3 className="text-xl font-bold text-slate-900">
                            댓글
                        </h3>

                        <span className="text-sm font-bold text-purple-500">
                            {commentPageInfo.totalElements}개
                        </span>
                    </div>

                    {/* 댓글 목록 */}
                    <div className="mt-5 space-y-4">
                        {pagedComments.length > 0 ? (
                            pagedComments.map((comment) => (
                                <div
                                    key={comment.commentId}
                                    className="rounded-2xl border border-gray-100 bg-slate-50 p-4"
                                >
                                    {/* 작성자 */}
                                    <p className="text-sm font-bold text-slate-700">
                                        {comment.nickname}
                                    </p>

                                    {/* 댓글 내용 */}
                                    <p className="mt-2 text-sm text-slate-600">
                                        {comment.content}
                                    </p>

                                    {/* 대댓글 작성 버튼 */}
                                    <button
                                        type="button"
                                        onClick={() => handleOpenReplyForm(comment.commentId)}
                                        className="mt-3 text-xs font-bold text-purple-500"
                                    >
                                        답글 작성
                                    </button>

                                    {/* 대댓글 목록 */}
                                    {(repliesMap[comment.commentId] || []).map((reply) => (
                                        <div
                                            key={reply.commentId}
                                            className="mt-3 ml-5 rounded-xl border border-purple-100 bg-white p-3"
                                        >
                                            <p className="text-xs font-bold text-slate-700">
                                                {reply.nickname}
                                            </p>

                                            <p className="mt-2 text-sm text-slate-600">
                                                {reply.content}
                                            </p>
                                        </div>
                                    ))}

                                    {/* 대댓글 작성창 */}
                                    {replyTargetId === comment.commentId && (
                                        <div className="mt-4 ml-5">
                                            <textarea
                                                value={replyContent}
                                                onChange={handleReplyChange}
                                                placeholder="대댓글을 입력하세요..."
                                                className="h-20 w-full resize-none rounded-xl border border-purple-100 bg-white p-3 text-sm outline-none"
                                            />

                                            <div className="mt-2 flex gap-2">
                                                <button
                                                    type="button"
                                                    onClick={() => handleCreateReply(comment.commentId)}
                                                    className="rounded-lg bg-purple-500 px-4 py-2 text-sm font-bold text-white"
                                                >
                                                    등록
                                                </button>

                                                <button
                                                    type="button"
                                                    onClick={handleCloseReplyForm}
                                                    className="rounded-lg bg-gray-200 px-4 py-2 text-sm font-bold text-gray-600"
                                                >
                                                    취소
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ))
                        ) : (
                            <p className="rounded-2xl border border-gray-100 bg-slate-50 p-4 text-sm text-gray-500">
                                아직 작성된 댓글이 없습니다.
                            </p>
                        )}
                    </div>

                    {/* 댓글 페이징 */}
                    {commentPageInfo.totalPages > 0 && (
                        <div className="mt-5 flex items-center justify-center gap-2">
                            <button
                                type="button"
                                disabled={commentPageInfo.first}
                                onClick={() => setCommentPage(commentPage - 1)}
                                className="rounded-full bg-gray-100 px-3 py-2 text-xs font-bold text-gray-600 disabled:cursor-not-allowed disabled:opacity-40"
                            >
                                이전
                            </button>

                            {Array.from({ length: commentPageInfo.totalPages }, (_, index) => (
                                <button
                                    key={index}
                                    type="button"
                                    onClick={() => setCommentPage(index)}
                                    className={`h-8 w-8 rounded-full text-xs font-bold ${
                                        commentPage === index
                                            ? "bg-purple-500 text-white"
                                            : "bg-gray-100 text-gray-600"
                                    }`}
                                >
                                    {index + 1}
                                </button>
                            ))}

                            <button
                                type="button"
                                disabled={commentPageInfo.last}
                                onClick={() => setCommentPage(commentPage + 1)}
                                className="rounded-full bg-gray-100 px-3 py-2 text-xs font-bold text-gray-600 disabled:cursor-not-allowed disabled:opacity-40"
                            >
                                다음
                            </button>
                        </div>
                    )}

                    {/* 댓글 작성 */}
                    {!showCommentForm ? (
                        <button
                            type="button"
                            onClick={handleOpenCommentForm}
                            className="mt-6 w-full rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 py-3 font-bold text-white transition hover:opacity-90"
                        >
                            댓글 작성하기
                        </button>
                    ) : (
                        <section className="mt-6 rounded-2xl border border-purple-100 bg-purple-50 p-5">
                            <h4 className="font-bold text-purple-700">
                                댓글 작성
                            </h4>

                            <textarea
                                value={commentContent}
                                onChange={handleCommentChange}
                                placeholder="댓글을 입력하세요..."
                                className="mt-4 h-24 w-full resize-none rounded-xl border border-purple-100 bg-white p-4 text-sm outline-none"
                            />

                            <button
                                type="button"
                                onClick={handleCreateComment}
                                className="mt-3 w-full rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 py-3 font-bold text-white transition hover:opacity-90"
                            >
                                등록하기
                            </button>
                        </section>
                    )}

                    {/* 관련 TIL - 현재는 UI용 정적 영역 */}
                    <section className="mt-8">
                        <h4 className="font-bold text-slate-900">
                            관련 TIL
                        </h4>

                        <ul className="mt-4 space-y-3 text-sm font-semibold text-slate-600">
                            <li>• JWT 토큰 구조 정리</li>
                            <li>• 로그인 API 구현 흐름</li>
                            <li>• 권한 처리와 Role 설계</li>
                        </ul>
                    </section>
                </aside>
            </div>
        </main>
    );
}

export default PostDetailPage;