// 게시글 관련 상수 관리 파일

// =========================
// 게시글 조회 관련
// =========================

// 게시글 기본 공개 여부
export const defaultVisibility = "PUBLIC";

// 게시글 카테고리 목록
export const categories = [
    "전체",
    "Java",
    "Spring",
    "DB",
    "Algorithm",
    "React",
];

// 게시글 난이도 색상 스타일
export const difficultyStyle = {
    EASY: "bg-[#E8F7E7] text-[#62C15B]",
    NORMAL: "bg-indigo-50 text-indigo-500",
    HARD: "bg-[#FDECEC] text-[#E44343]",
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