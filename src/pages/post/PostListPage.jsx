import { usePostList } from "../../hooks/post/usePostList";

import { difficultyStyle, difficultyBorderStyle } from "../../constants/post";

// 게시글 목록 페이지
function PostListPage() {
    const {
        posts,
        handleMoveWrite,
        handleMoveDetail,
        handleSearchTag,
    } = usePostList();

    return (
        <main className="space-y-7">
            {/* 상단 검색/소개 영역 */}
            <section className="rounded-3xl border-2 border-cyan-400 bg-gradient-to-r from-purple-50 to-cyan-50 p-8">
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-3xl font-bold">
                            TIL 목록
                        </h2>

                        <p className="mt-2 text-gray-600">
                            작성된 TIL을 검색하고 난이도별로 학습 기록을 탐색하세요.
                        </p>
                    </div>

                    <div className="flex items-center gap-3">
                        <input
                            type="text"
                            placeholder="검색어를 입력하세요"
                            className="w-72 rounded-xl border border-gray-200 bg-white px-5 py-3 text-sm outline-none"
                        />

                        <button className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-6 py-3 font-bold text-white">
                            검색
                        </button>
                    </div>
                </div>
            </section>

            {/* 난이도 필터 */}
            <div className="flex gap-3">
                {["전체", "EASY", "NORMAL", "HARD"].map((difficulty) => (
                    <button
                        key={difficulty}
                        className="rounded-full bg-gray-100 px-6 py-2 text-sm font-bold text-gray-600 transition hover:bg-purple-500 hover:text-white"
                    >
                        {difficulty}
                    </button>
                ))}
            </div>

            <section className="grid grid-cols-[1fr_280px] gap-7">
                {/* 게시글 목록 영역 */}
                <div className="rounded-3xl bg-white p-8 shadow-sm">
                    <h3 className="mb-6 text-xl font-bold">
                        전체 게시글 {posts.length}개
                    </h3>

                    <div className="space-y-4">
                        {posts.map((post) => (
                            <div
                                key={post.postId}
                                onClick={() => handleMoveDetail(post.postId)}
                                className={`cursor-pointer rounded-2xl border border-gray-100 border-l-4 bg-white p-5 shadow-sm transition hover:shadow-md ${
                                    difficultyBorderStyle[post.difficulty] || difficultyBorderStyle.NORMAL
                                }`}
                            >
                                <div className="flex items-center justify-between gap-5">
                                    <div className="min-w-0">
                                        <div className="mb-2 flex flex-wrap gap-2">
                                            {post.tagNames.map((tag) => (
                                                <button
                                                    key={tag}
                                                    type="button"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        handleSearchTag(tag);
                                                    }}
                                                    className="rounded-full bg-purple-100 px-3 py-1 text-xs font-bold text-purple-600"
                                                >
                                                    #{tag}
                                                </button>
                                            ))}
                                        </div>

                                        <h4 className="text-lg font-bold">
                                            {post.title}
                                        </h4>

                                        <p className="mt-2 text-sm text-gray-500">
                                            난이도 {post.difficulty}
                                            · 작성자 {post.nickname}
                                            · 조회수 {post.viewCount}회
                                            · 학습시간 {post.studyTime}분
                                        </p>
                                    </div>

                                    <span
                                        className={`shrink-0 rounded-full px-7 py-2 text-sm font-bold ${
                                            difficultyStyle[post.difficulty] || difficultyStyle.NORMAL
                                        }`}
                                    >
                                        {post.difficulty}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="mt-16 text-center font-bold text-gray-500">
                        ‹ 이전 1 2 3 4 5 다음 ›
                    </div>
                </div>

                {/* 우측 보조 정보 */}
                <aside className="rounded-3xl bg-white p-7 shadow-sm">
                    <h3 className="text-xl font-bold">
                        목록 보조 정보
                    </h3>

                    <div className="mt-6 space-y-4">
                        <div className="rounded-2xl border border-gray-100 p-5">
                            <p className="text-sm font-bold text-gray-500">
                                현재 조회 게시글
                            </p>
                            <p className="mt-2 text-3xl font-bold">
                                {posts.length}개
                            </p>
                        </div>

                        <div className="rounded-2xl border border-gray-100 p-5">
                            <p className="text-sm font-bold text-gray-500">
                                평균 학습시간
                            </p>
                            <p className="mt-2 text-3xl font-bold">
                                {posts.length > 0
                                    ? Math.round(
                                        posts.reduce((sum, post) => sum + (post.studyTime || 0), 0) / posts.length
                                    )
                                    : 0}
                                <span className="text-base">분</span>
                            </p>
                        </div>
                    </div>

                    <div className="mt-8">
                        <p className="mb-3 font-bold">
                            정렬 옵션
                        </p>

                        <div className="grid grid-cols-2 gap-3">
                            <button className="rounded-full bg-gray-100 py-2 text-sm font-bold text-gray-600">
                                최신순
                            </button>
                            <button className="rounded-full bg-gray-100 py-2 text-sm font-bold text-gray-600">
                                조회순
                            </button>
                            <button className="rounded-full bg-gray-100 py-2 text-sm font-bold text-gray-600">
                                학습시간순
                            </button>
                            <button className="rounded-full bg-gray-100 py-2 text-sm font-bold text-gray-600">
                                난이도순
                            </button>
                        </div>
                    </div>
                </aside>
            </section>

            {/* 하단 응원 문구 */}
            <section className="rounded-2xl bg-gradient-to-r from-purple-100 to-cyan-100 p-6">
                <p className="text-lg font-bold">
                    꾸준함이 최고의 실력입니다!
                </p>
                <p className="mt-1 text-sm text-gray-600">
                    오늘도 기록하는 당신을 응원합니다. 작은 기록이 성장의 증거가 됩니다.
                </p>
            </section>
        </main>
    );
}

export default PostListPage;