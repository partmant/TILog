import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { recordWriteHistory } from "../../api/myPageApi";
import { createPost, getPostDetail, updatePost , uploadPostImage } from "../../api/post";
import { getMemberId } from "../../utils/authUtils";

import { defaultDifficulty, defaultVisibility, postListPath } from "../../constants/post";

// 게시글 작성/수정 관련 로직 관리 Hook

export function usePostWriteForm() {
    // =========================
    // 라우팅 관련
    // =========================
    const navigate = useNavigate();
    const { postId } = useParams();
    const isEditMode = Boolean(postId);

    // =========================
    // 상태 관리
    // =========================
    const editorRef = useRef(null);
    const [form, setForm] = useState({
        title: "",
        difficulty: defaultDifficulty,
        studyTime: "",
        visibility: defaultVisibility,
        tags: "",
    });

    // =========================
    // 이벤트 처리
    // =========================

    // 게시글 입력값 변경 처리
    const handleChange = (e) => {
        const { name, value } = e.target;

        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    // 코드블록 언어 선택 시 Markdown 코드블록 삽입
    const handleInsertCodeBlock = (language) => {
        if (!language) return;

        const editor = editorRef.current?.getInstance();

        if (!editor) return;

        editor.insertText(`\`\`\`${language}\n\n\`\`\``);

        setTimeout(() => {
            const currentEditor = editor.getCurrentModeEditor();

            currentEditor.setSelection([2, 1], [2, 1]);

            editor.focus();
        }, 0);
    };

    // 게시글 이미지 업로드
    const handleUploadImage = async (blob, callback) => {
        try {
            const formData = new FormData();

            formData.append("image", blob);

            const result = await uploadPostImage(formData);

            callback(
                result.imageUrl,
                "게시글 이미지"
            );

        } catch (error) {
            console.error(error);

            alert("이미지 업로드 실패");
        }
    };

    // 에디터 로드 후 신규 작성 본문 초기화
    const handleEditorLoad = (editor) => {
        if (isEditMode) return;

        editor.setMarkdown("");
    };

    // =========================
    // 데이터 조회
    // =========================

    // 수정 모드일 경우 게시글 상세 조회
    useEffect(() => {
        if (!isEditMode) return;

        const fetchPostDetail = async () => {
            const data = await getPostDetail(postId, false);

            setForm({
                title: data.title,
                difficulty: data.difficulty,
                visibility: data.visibility ?? defaultVisibility,
                studyTime: data.studyTime ?? "",
                tags: data.tagNames?.join(", ") ?? "",
            });

            const editor = editorRef.current?.getInstance();

            if (editor) {
                editor.setMarkdown(data.content || "");
            }
        };

        fetchPostDetail();
    }, [isEditMode, postId]);

    // =========================
    // API 요청
    // =========================

    // 게시글 저장 요청 공통 처리
    const savePost = async (visibility) => {
        const editor = editorRef.current?.getInstance();

        if (!editor) {
            alert("에디터가 준비되지 않았습니다.");
            return;
        }

        const content = editor.getMarkdown();

        const tagNames = form.tags
            .split(",")
            .map((tag) => tag.trim())
            .filter((tag) => tag.length > 0);

        const { tags, ...postForm } = form;

        const postData = {
            ...postForm,
            visibility,
            content,
            studyTime: Number(form.studyTime),
            tagNames,
        };

        if (isEditMode) {
            await updatePost(postId, postData);
            navigate(`/posts/${postId}`);
            return;
        }

        await createPost(postData);

        if (visibility !== "DRAFT") {
            const memberId = getMemberId();

            if (memberId) {
                try {
                    await recordWriteHistory(memberId);
                } catch (error) {
                    console.error("[WRITE HISTORY API ERROR]", error);
                }
            }
        }

        navigate(postListPath);
    };

    // 게시글 작성 또는 수정 요청
    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            await savePost(form.visibility);
        } catch (error) {
            console.error(error);
            alert(isEditMode ? "게시글 수정 실패" : "게시글 작성 실패");
        }
    };

    // 임시저장 요청
    const handleTempSave = async () => {
        try {
            // 임시저장은 visibility를 DRAFT로 고정
            await savePost("DRAFT");

            alert("임시저장되었습니다.");
        } catch (error) {
            console.error(error);

            alert("임시저장 실패");
        }
    };

    // 게시글 작성 취소 시 게시글 목록으로 이동
    const handleCancel = () => {
        navigate(postListPath);
    };

    return {
        form,
        editorRef,
        editorKey: isEditMode ? `edit-${postId}` : "write",
        isEditMode,
        handleChange,
        handleInsertCodeBlock,
        handleUploadImage,
        handleEditorLoad,
        handleSubmit,
        handleTempSave,
        handleCancel,
    };
}
