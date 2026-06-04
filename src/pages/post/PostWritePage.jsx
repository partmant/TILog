import { useEffect, useState } from "react";
import { Editor } from "@toast-ui/react-editor";
import "@toast-ui/editor/dist/toastui-editor.css";
import "@toast-ui/editor/dist/i18n/ko-kr";

import { getMyStreak } from "../../api/myPageApi";
import { usePostWriteForm } from "../../hooks/post";
import { getCurrentStreak } from "../../utils/growthStats";
import { TEMP_MEMBER_ID } from "../../utils/mypageUtils";

import {
    difficultyOptions,
    markdownPlaceholder,
    visibilityOptions,
    editorHeight,
    editorToolbarItems,
    codeBlockSelectLabel,
    codeBlockLanguageOptions,
} from "../../constants/post";

// 게시글 작성 페이지
function PostWritePage() {
    const [currentStreak, setCurrentStreak] = useState(0);
    const [isStreakLoading, setIsStreakLoading] = useState(true);

    const {
        form,
        editorRef,
        editorKey,
        isEditMode,
        handleChange,
        handleInsertCodeBlock,
        handleUploadImage,
        handleEditorLoad,
        handleSubmit,
        handleTempSave,
        handleCancel,
    } = usePostWriteForm();

    useEffect(() => {
        const fetchCurrentStreak = async () => {
            try {
                setIsStreakLoading(true);

                const streakResponse = await getMyStreak({
                    memberId: TEMP_MEMBER_ID,
                    useCache: true,
                });

                setCurrentStreak(getCurrentStreak(streakResponse));
            } catch (error) {
                console.error("[STREAK API ERROR]", error);
                setCurrentStreak(0);
            } finally {
                setIsStreakLoading(false);
            }
        };

        fetchCurrentStreak();
    }, []);

    return (
        <main className="rounded-3xl bg-white/90 p-8 shadow-sm">
            {/* 작성 페이지 전체 박스 */}
            <section className="rounded-3xl border-2 border-cyan-400 bg-white p-8">
                {/* 상단 제목 영역 */}
                <div>
                    <h2 className="text-3xl font-bold">
                        {isEditMode ? "TIL 수정하기" : "TIL 작성하기"}
                    </h2>

                    <p className="mt-2 text-gray-500">
                        {isEditMode
                            ? "작성한 TIL 내용을 수정하세요"
                            : "오늘 배운 내용을 기록하면 작성 이력과 스트릭에 자동 반영됩니다."}
                    </p>
                </div>

                {/* 게시글 작성 폼 */}
                <form onSubmit={handleSubmit} className="mt-8">
                    <div className="grid grid-cols-[1fr_300px] gap-8">
                        {/* 왼쪽 입력 영역 */}
                        <div className="space-y-7">
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
                                    placeholder="오늘 배운 내용을 한 줄로 정리하세요"
                                    className="w-full rounded-2xl border border-gray-200 px-5 py-4 outline-none transition focus:border-purple-400"
                                />
                            </div>

                            {/* 태그 */}
                            <div>
                                <label className="mb-2 block text-sm font-bold text-gray-700">
                                    태그
                                </label>

                                <input
                                    type="text"
                                    name="tags"
                                    value={form.tags}
                                    onChange={handleChange}
                                    placeholder="예: Spring, JPA, React"
                                    className="w-full rounded-2xl border border-gray-200 px-5 py-4 outline-none transition focus:border-purple-400"
                                />

                                <p className="mt-2 text-sm text-gray-400">
                                    쉼표(,)로 구분하여 입력해주세요.
                                </p>
                            </div>

                            {/* 본문 */}
                            <div>
                                <label className="mb-2 block text-sm font-bold text-gray-700">
                                    내용
                                </label>

                                <div className="relative overflow-hidden rounded-2xl border border-gray-200">
                                    {/* 코드블록 언어 선택 */}
                                    <select
                                        defaultValue=""
                                        onChange={(e) => {
                                            handleInsertCodeBlock(e.target.value);

                                            // 선택 후 초기화
                                            e.target.value = "";
                                        }}
                                        className="absolute right-3 top-12 z-10 h-8 rounded-md border border-gray-200 bg-white px-2 text-sm outline-none"
                                    >
                                        <option value="" disabled>
                                            {codeBlockSelectLabel}
                                        </option>

                                        {codeBlockLanguageOptions.map((option) => (
                                            <option key={option.value} value={option.value}>
                                                {option.label}
                                            </option>
                                        ))}
                                    </select>

                                    <Editor
                                        key={editorKey}
                                        ref={editorRef}
                                        initialValue=""
                                        placeholder={markdownPlaceholder}
                                        previewStyle="tab"
                                        height={editorHeight}
                                        initialEditType="markdown"
                                        useCommandShortcut={true}
                                        language="ko-KR"
                                        toolbarItems={editorToolbarItems}
                                        onLoad={handleEditorLoad}
                                        hooks={{ addImageBlobHook: handleUploadImage }}
                                    />
                                </div>
                            </div>
                        </div>

                        {/* 오른쪽 작성 보조 패널 */}
                        <aside className="rounded-3xl border border-gray-200 bg-white p-6 shadow-sm">
                            <h3 className="text-xl font-bold">
                                작성 보조 패널
                            </h3>

                            <p className="mt-2 text-sm text-gray-500">
                                오늘 작성하면 학습 기록에 반영됩니다.
                            </p>

                            {/* 난이도 */}
                            <div className="mt-6">
                                <label className="mb-2 block text-sm font-bold text-gray-700">
                                    난이도
                                </label>

                                <select
                                    name="difficulty"
                                    value={form.difficulty}
                                    onChange={handleChange}
                                    className="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none transition focus:border-purple-400"
                                >
                                    {difficultyOptions.map((option) => (
                                        <option key={option} value={option}>
                                            {option}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* 학습 시간 */}
                            <div className="mt-5">
                                <label className="mb-2 block text-sm font-bold text-gray-700">
                                    학습 시간 (분)
                                </label>

                                <input
                                    type="number"
                                    name="studyTime"
                                    value={form.studyTime}
                                    onChange={handleChange}
                                    placeholder="예: 60"
                                    className="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none transition focus:border-purple-400"
                                />
                            </div>

                            {/* 공개 여부 */}
                            <div className="mt-5">
                                <label className="mb-2 block text-sm font-bold text-gray-700">
                                    공개 여부
                                </label>

                                <select
                                    name="visibility"
                                    value={form.visibility}
                                    onChange={handleChange}
                                    className="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none transition focus:border-purple-400"
                                >
                                    {visibilityOptions.map((option) => (
                                        <option key={option.value} value={option.value}>
                                            {option.label}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {/* 스트릭 카드 */}
                            <div className="mt-7 rounded-2xl border border-gray-100 bg-gradient-to-r from-purple-50 to-cyan-50 p-5">
                                <p className="text-sm font-bold text-gray-600">
                                    현재 스트릭
                                </p>

                                <p className="mt-2 text-3xl font-bold">
                                    {isStreakLoading ? "..." : currentStreak}
                                    <span className="text-base">일</span>
                                </p>

                                <p className="mt-1 text-xs text-gray-500">
                                    내일도 이어가세요
                                </p>
                            </div>

                            {/* 체크리스트 */}
                            <div className="mt-7">
                                <h4 className="font-bold">
                                    체크리스트
                                </h4>

                                <ul className="mt-4 space-y-3 text-sm font-semibold text-gray-600">
                                    <li>☑ 제목을 구체적으로 작성</li>
                                    <li>☑ 핵심 개념 3개 이상 정리</li>
                                    <li>☑ 복습할 내용 남기기</li>
                                </ul>
                            </div>
                        </aside>
                    </div>

                    {/* 버튼 영역 */}
                    <div className="mt-10 flex justify-end gap-4">
                        {/* 취소 버튼 */}
                        <button
                            type="button"
                            onClick={handleCancel}
                            className="rounded-2xl border border-gray-200 px-8 py-3 font-bold text-gray-600 transition hover:bg-gray-100"
                        >
                            취소
                        </button>

                        {/* 임시 저장 버튼 */}
                        <button
                            type="button"
                            onClick={handleTempSave}
                            className="rounded-2xl bg-gray-100 px-8 py-3 font-bold text-gray-600 transition hover:bg-gray-200"
                        >
                            임시 저장
                        </button>

                        {/* 작성 버튼 */}
                        <button
                            type="submit"
                            className="rounded-2xl bg-gradient-to-r from-purple-500 to-cyan-400 px-10 py-3 font-bold text-white transition hover:opacity-90"
                        >
                            {isEditMode ? "수정 완료" : "게시하기"}
                        </button>
                    </div>
                </form>
            </section>
        </main>
    );
}

export default PostWritePage;
