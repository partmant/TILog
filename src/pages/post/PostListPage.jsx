import { usePostList } from "../../hooks/post/usePostList";

import { difficultyStyle, difficultyBorderStyle } from "../../constants/post";

// 게시글 목록 페이지
function PostListPage() {
    const {
        posts,
        handleMoveWrite,
        handleMoveDetail,
    } = usePostList();

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
                    onClick={handleMoveWrite}
                    className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-8 py-3 font-bold text-white"
                >
                    TIL 작성하기
                </button>
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
                        onClick={() => handleMoveDetail(post.postId)}

                        // 게시글 난이도별 색상 구분
                        className={`cursor-pointer rounded-2xl border border-gray-100 border-l-4 bg-white p-6 shadow-sm transition hover:shadow-md ${
                            difficultyBorderStyle[post.difficulty] || difficultyBorderStyle.NORMAL
                        }`}
                    >
                        <div className="flex items-center justify-between">
                            <div>
                                {/* 게시글 제목 */}
                                <h3 className="text-xl font-bold">
                                    {post.title}
                                </h3>

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

                                {/* 게시글 정보 */}
                                <p className="mt-3 text-sm text-gray-500">
                                    난이도 {post.difficulty}
                                    · 작성자 {post.nickname}
                                    · 조회수 {post.viewCount}회
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