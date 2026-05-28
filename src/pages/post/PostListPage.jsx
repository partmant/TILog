import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getPostList } from "../../api/post";
import { categories, difficultyStyle } from "../../constants/post";

// 게시글 목록 페이지

function PostListPage() {
    // 페이지 이동 객체
    const navigate = useNavigate();

    // 게시글 목록 상태
    const [posts, setPosts] = useState([]);

    // 게시글 목록 조회
    useEffect(() => {
        const fetchPosts = async () => {
            const data = await getPostList();
            setPosts(data);
        };

        fetchPosts();
    }, []);

    return (
        <main className="rounded-3xl bg-white/90 p-8 shadow-sm">
            {/* 상단 영역 */}
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-3xl font-bold">
                        TIL 게시글 목록
                    </h2>

                    <p className="mt-2 text-gray-500">
                        기술 스택별 학습 기록을 탐색하세요
                    </p>
                </div>

                {/* 게시글 작성 페이지 이동 */}
                <button
                    onClick={() => navigate("/posts/write")}
                    className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-8 py-3 font-bold text-white"
                >
                    TIL 작성하기
                </button>
            </div>

            {/* 카테고리 영역 */}
            <div className="mt-8 flex gap-3">
                {categories.map((category) => (
                    <button
                        key={category}
                        className={`rounded-2xl px-5 py-2 text-sm font-bold ${
                            category === "전체"
                                ? "bg-purple-500 text-white"
                                : "bg-slate-100 text-slate-800"
                        }`}
                    >
                        {category}
                    </button>
                ))}
            </div>

            {/* 정렬 영역 */}
            <p className="mt-6 font-bold text-gray-600">
                정렬: 최신순 ▼
            </p>

            {/* 게시글 목록 */}
            <div className="mt-7 space-y-5">
                {posts.map((post) => (
                    // 게시글 카드
                    <div
                        key={post.postId}

                        // 게시글 상세 페이지 이동
                        onClick={() => navigate(`/posts/${post.postId}`)}

                        className="cursor-pointer rounded-2xl border border-gray-100 border-l-4 border-l-indigo-500 bg-white p-6 shadow-sm transition hover:shadow-md"
                    >
                        <div className="flex items-center justify-between">
                            <div>
                                {/* 게시글 제목 */}
                                <h3 className="text-xl font-bold">
                                    {post.title}
                                </h3>

                                {/* 게시글 정보 */}
                                <p className="mt-3 text-sm text-gray-500">
                                    난이도 {post.difficulty}
                                    · 작성자 {post.nickname}
                                    · 조회수 {post.viewCount}
                                    · 학습시간 {post.studyTime}분
                                </p>
                            </div>

                            {/* 난이도 뱃지 */}
                            <span
                                className={`rounded-full px-8 py-2 text-sm font-bold ${
                                    difficultyStyle[post.difficulty] || difficultyStyle.NORMAL
                                }`}
                            >
                                {post.difficulty}
                            </span>
                        </div>
                    </div>
                ))}
            </div>

            {/* 페이지네이션 */}
            <div className="mt-20 text-center font-bold text-gray-500">
                ‹ 이전 1 2 3 4 5 다음 ›
            </div>
        </main>
    );
}

export default PostListPage;