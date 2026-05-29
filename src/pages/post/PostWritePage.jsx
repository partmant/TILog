import { Editor } from "@toast-ui/react-editor";
import "@toast-ui/editor/dist/toastui-editor.css";
import "@toast-ui/editor/dist/i18n/ko-kr";

import { usePostWriteForm } from "../../hooks/post/usePostWriteForm";

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
        handleCancel,
    } = usePostWriteForm();

    return (
        <main className="rounded-3xl bg-white/90 p-8 shadow-sm">
            {/* 상단 영역 */}
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-3xl font-bold">
                        {isEditMode ? "TIL 수정하기" : "TIL 작성하기"}
                    </h2>

                    <p className="mt-2 text-gray-500">
                        {isEditMode
                            ? "작성한 TIL 내용을 수정하세요"
                            : "오늘 학습한 내용을 기록해보세요"}
                    </p>
                </div>
            </div>

            {/* 게시글 작성 폼 */}
            <form onSubmit={handleSubmit} className="mt-10 space-y-7">
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
                            <option key={option} value={option}>
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
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
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
                            className="absolute right-3 top-2 z-10 h-8 rounded-md border border-gray-200 bg-white px-2 text-sm outline-none"
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
                            hooks={{addImageBlobHook: handleUploadImage,}}
                        />
                    </div>
                </div>

                {/* 버튼 영역 */}
                <div className="flex justify-end gap-3">
                    {/* 취소 버튼 */}
                    <button
                        type="button"
                        onClick={handleCancel}
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
