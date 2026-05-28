import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";

import "highlight.js/styles/github-dark.css";

import { createPost, getPostDetail, updatePost } from "../../api/post";

import {
    difficultyOptions,
    defaultDifficulty,
    markdownPlaceholder,
    postListPath,
    visibilityOptions,
    defaultVisibility,
} from "../../constants/post";

// 게시글 작성 페이지
function PostWritePage() {
    // 페이지 이동 객체
    const navigate = useNavigate();

    // URL 게시글 ID
    const { postId } = useParams();

    // 수정 모드 여부
    const isEditMode = Boolean(postId);

    // 게시글 폼 상태
    const [form, setForm] = useState({
        title: "",
        content: "",
        difficulty: defaultDifficulty,
        studyTime: "",
        visibility: defaultVisibility,
    });

    // 입력값 변경
    const handleChange = (e) => {
        const { name, value } = e.target;

        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    // 수정 모드 게시글 데이터 조회
    useEffect(() => {
        // 작성 모드면 종료
        if (!isEditMode) return;

        const fetchPostDetail = async () => {
            const data = await getPostDetail(postId);

            setForm({
                title: data.title,
                content: data.content,
                difficulty: data.difficulty,
                visibility: data.visibility ?? defaultVisibility,
                studyTime: data.studyTime ?? "",
            });
        };

        fetchPostDetail();
    }, [isEditMode, postId]);

    // 게시글 작성/수정 요청
    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const postData = {
                ...form,
                studyTime: Number(form.studyTime),
            };

            // 수정 모드
            if (isEditMode) {
                await updatePost(postId, postData);

                navigate(`/posts/${postId}`);
            } else {
                // 작성 모드
                await createPost(postData);

                navigate(postListPath);
            }
        } catch (error) {
            console.error(error);

            alert(
                isEditMode
                    ? "게시글 수정 실패"
                    : "게시글 작성 실패"
            );
        }
    };

    return (
        <main className="rounded-3xl bg-white/90 p-8 shadow-sm">
            {/* 상단 영역 */}
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-3xl font-bold">
                        {isEditMode ? "TIL 수정하기" : "TIL 작성하기"}
                    </h2>

                    <p className="mt-2 text-gray-500">
                        {isEditMode ? "작성한 TIL 내용을 수정하세요" : "오늘 학습한 내용을 기록해보세요"}
                    </p>
                </div>
            </div>

            {/* 게시글 작성 폼 */}
            <form
                onSubmit={handleSubmit}
                className="mt-10 space-y-7"
            >
                {/* 제목 */}
                <div>
                    <label className="mb-2 block text-sm font-bold text-gray-700">
                        제목
                    </label>

                    <input
                        type="text"
                        name="title"
                        value={form.title}
                        onChange={handleChange}
                        placeholder="게시글 제목을 입력하세요"
                        className="w-full rounded-2xl border border-gray-200 px-5 py-4 outline-none"
                    />
                </div>

                {/* 난이도 */}
                <div>
                    <label className="mb-2 block text-sm font-bold text-gray-700">
                        난이도
                    </label>

                    <select
                        name="difficulty"
                        value={form.difficulty}
                        onChange={handleChange}
                        className="w-full rounded-2xl border border-gray-200 px-5 py-4 outline-none"
                    >
                        {difficultyOptions.map((option) => (
                            <option
                                key={option}
                                value={option}
                            >
                                {option}
                            </option>
                        ))}
                    </select>
                </div>

                {/* 학습 시간 */}
                <div>
                    <label className="mb-2 block text-sm font-bold text-gray-700">
                        학습 시간 (분)
                    </label>

                    <input
                        type="number"
                        name="studyTime"
                        value={form.studyTime}
                        onChange={handleChange}
                        placeholder="학습 시간 입력"
                        className="w-full rounded-2xl border border-gray-200 px-5 py-4 outline-none"
                    />
                </div>

                {/* 공개 여부 */}
                <div>
                    <label className="mb-2 block text-sm font-bold text-gray-700">
                        공개 여부
                    </label>

                    <select
                        name="visibility"
                        value={form.visibility}
                        onChange={handleChange}
                        className="w-full rounded-2xl border border-gray-200 px-5 py-4 outline-none"
                    >
                        {visibilityOptions.map((option) => (
                            <option
                                key={option.value}
                                value={option.value}
                            >
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>

                {/* 본문 + Markdown Preview */}
                <div>
                    <label className="mb-2 block text-sm font-bold text-gray-700">
                        내용
                    </label>

                    <div
                        className={`grid gap-6 ${
                            form.content.trim() ? "grid-cols-2" : "grid-cols-1"
                        }`}
                    >
                        {/* Markdown 작성 영역 */}
                        <textarea
                            name="content"
                            value={form.content}
                            onChange={handleChange}
                            placeholder={markdownPlaceholder}
                            rows={18}
                            className="rounded-2xl border border-gray-200 px-5 py-4 font-mono outline-none"
                        />

                        {/* Markdown Preview - 내용 입력 시에만 표시 */}
                        {form.content.trim() && (
                            <div className="prose max-w-none overflow-y-auto rounded-2xl border border-gray-200 bg-slate-50 p-5">
                                <ReactMarkdown
                                    remarkPlugins={[remarkGfm]}
                                    rehypePlugins={[rehypeHighlight]}
                                >
                                    {form.content}
                                </ReactMarkdown>
                            </div>
                        )}
                    </div>
                </div>

                {/* 버튼 영역 */}
                <div className="flex justify-end gap-3">
                    {/* 취소 버튼 */}
                    <button
                        type="button"
                        onClick={() => navigate(postListPath)}
                        className="rounded-2xl border px-7 py-3 font-bold"
                    >
                        취소
                    </button>

                    {/* 작성 버튼 */}
                    <button
                        type="submit"
                        className="rounded-2xl bg-gradient-to-r from-purple-500 to-cyan-400 px-7 py-3 font-bold text-white"
                    >
                        {isEditMode ? "수정 완료" : "작성 완료"}
                    </button>
                </div>
            </form>
        </main>
    );
}

export default PostWritePage;