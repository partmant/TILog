// 게시글 관련 상수 관리 파일

// =========================
// 게시글 조회 관련
// =========================

// 게시글 기본 공개 여부
export const defaultVisibility = "PUBLIC";

// 게시글 난이도 색상 스타일
export const difficultyStyle = {
    EASY: "bg-[#E8F7E7] text-[#62C15B]",
    NORMAL: "bg-indigo-50 text-indigo-500",
    HARD: "bg-[#FDECEC] text-[#E44343]",
};

// 게시글 난이도 왼쪽 테두리 스타일
export const difficultyBorderStyle = {
    EASY: "border-l-[#62C15B]",
    NORMAL: "border-l-indigo-500",
    HARD: "border-l-[#E44343]",
};

// =========================
// 게시글 작성 관련
// =========================

// 게시글 난이도 옵션
export const difficultyOptions = [
    "EASY",
    "NORMAL",
    "HARD",
];

// 게시글 기본 난이도
export const defaultDifficulty = "NORMAL";

// 게시글 공개 여부 옵션
export const visibilityOptions = [
    {
        value: "PUBLIC",
        label: "공개",
    },
    {
        value: "PRIVATE",
        label: "비공개",
    },
];

// 게시글 작성 완료 후 이동 경로
export const postListPath = "/posts";

// 게시글 상세 페이지 경로 생성
export const getPostDetailPath = (postId) => `/posts/${postId}`;

// 게시글 수정 페이지 경로 생성
export const getPostEditPath = (postId) => `/posts/${postId}/edit`;

// 게시글 Markdown 작성 예시
export const markdownPlaceholder = `# 제목을 입력하세요

학습한 내용을 Markdown 형식으로 정리해보세요.

## 예시 코드

\`\`\`c
#include <stdio.h>

int main() {
    printf("Hello World");
    return 0;
}
\`\`\`
`;

// =========================
// Toast UI Editor 관련
// =========================

// Toast UI Editor 높이
export const editorHeight = "500px";

// Toast UI Editor 툴바 설정
export const editorToolbarItems = [
    // 제목 / 글자 스타일
    ["heading", "bold", "italic", "strike"],

    // 구분선 / 인용문
    ["hr", "quote"],

    // 목록
    ["ul", "ol", "task"],

    // 들여쓰기
    ["indent", "outdent"],

    // 표 / 이미지 / 링크
    ["table", "image", "link"],

    // 인라인 코드 / 코드블록
    ["code", "codeblock"],
];

// 코드블록 선택 기본 라벨
export const codeBlockSelectLabel = "코드블록";

// 코드블록 언어 옵션
export const codeBlockLanguageOptions = [
    { value: "c", label: "C",},
    { value: "java", label: "Java",},
    { value: "javascript", label: "JavaScript",},
    { value: "jsx", label: "JSX",},
    { value: "html", label: "HTML",},
    { value: "css", label: "CSS",},
    { value: "sql", label: "SQL",},
    { value: "bash", label: "Bash",},
    { value: "json", label: "JSON",},
];
