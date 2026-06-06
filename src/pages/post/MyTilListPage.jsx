import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useMyTilList } from "../../hooks/post";
import { difficultyStyle, difficultyBorderStyle } from "../../constants/post";
import SearchBar from "../../components/search/SearchBar.jsx";
import AdvancedSearchPanel from "../../components/search/AdvancedSearchPanel.jsx";

// 내 TIL 전체 보기 페이지 — 검색/필터/정렬 포함
function MyTilListPage() {
    const navigate = useNavigate();
    const searchWrapperRef = useRef(null);

    // 페이지 진입 및 페이지 번호 변경 시 최상단으로 스크롤
    useEffect(() => {
        window.scrollTo({ top: 0, behavior: "instant" });
    }, []);

    const {
        posts,
        pageInfo,
        currentPage,
        handleMoveDetail,
        handleSearchTag,
        selectedDifficulty,
        setSelectedDifficulty,
        sortType,
        setSortType,
        setPage,
        conditions,
        setCondition,
        toggleAdvanced,
        resetConditions,
    } = useMyTilList();

    // 페이지 번호 변경 시 최상단으로 스크롤
    useEffect(() => {
        window.scrollTo({ top: 0, behavior: "instant" });
    }, [currentPage]);

    // 상세검색 패널 외부 클릭 시 닫기
    useEffect(() => {
        if (!conditions.advanced) return;

        const handler = (e) => {
            if (searchWrapperRef.current && !searchWrapperRef.current.contains(e.target)) {
                toggleAdvanced();
            }
        };

        document.addEventListener("mousedown", handler);
        return () => document.removeEventListener("mousedown", handler);
    }, [conditions.advanced, toggleAdvanced]);

    const totalPages = pageInfo.totalPages || 0;
    const pageWindowSize = 5;
    const pageWindowStart = Math.max(
        0,
        Math.min(currentPage - 2, Math.max(totalPages - pageWindowSize, 0))
    );
    const pageNumbers = Array.from(
        { length: Math.min(totalPages, pageWindowSize) },
        (_, i) => pageWindowStart + i
    );

    return (
        <main className="space-y-7">
            {/* 상단 헤더 배너 */}
            <section className="rounded-3xl border-2 border-cyan-400 bg-gradient-to-r from-purple-50 to-cyan-50 p-8">
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-3xl font-bold">내 TIL 전체 보기</h2>
                        <p className="mt-2 text-gray-600">내가 작성한 TIL을 검색하고 필터링하세요.</p>
                    </div>
                    <button
                        type="button"
                        onClick={() => navigate("/mypage")}
                        className="rounded-xl border border-gray-200 bg-white px-5 py-2 text-sm font-bold text-gray-500 transition hover:bg-gray-50"
                    >
                        ← 돌아가기
                    </button>
                </div>
            </section>

            {/* 검색바 — 헤더 섹션 밖에 배치해 패널 overflow 방지 */}
            <div className="flex justify-end">
                <div className="relative" ref={searchWrapperRef}>
                    <SearchBar
                        keyword={conditions.keyword}
                        advanced={conditions.advanced}
                        onSearch={(kw) => setCondition("keyword", kw)}
                        onToggleAdvanced={toggleAdvanced}
                    />
                    {conditions.advanced && (
                        <div className="absolute right-0 top-full z-20 mt-2 w-[440px]">
                            <AdvancedSearchPanel
                                conditions={conditions}
                                onChange={setCondition}
                                onReset={resetConditions}
                                showNickname={false}
                            />
                        </div>
                    )}
                </div>
            </div>

            {/* 난이도 필터 (왼쪽) + 정렬 옵션 (오른쪽) */}
            <div className="flex items-center justify-between">
                <div className="flex gap-3">
                    {["ALL", "EASY", "NORMAL", "HARD"].map((d) => (
                        <button
                            key={d}
                            type="button"
                            onClick={() => setSelectedDifficulty(d)}
                            className={`rounded-full px-6 py-2 text-sm font-bold transition ${
                                selectedDifficulty === d
                                    ? "bg-purple-500 text-white"
                                    : "bg-gray-100 text-gray-600 hover:bg-purple-500 hover:text-white"
                            }`}
                        >
                            {d === "ALL" ? "전체" : d}
                        </button>
                    ))}
                </div>

                <div className="flex items-center gap-2">
                    <span className="text-sm font-bold text-gray-400">정렬</span>
                    <div className="mx-1 h-5 w-px bg-gray-300" />
                    {[
                        { value: "LATEST",   label: "최신순" },
                        { value: "VIEWS",    label: "조회수순" },
                        { value: "COMMENTS", label: "댓글순" },
                    ].map((opt) => (
                        <button
                            key={opt.value}
                            type="button"
                            onClick={() => setSortType(opt.value)}
                            className={`rounded-full px-5 py-2 text-sm font-bold transition ${
                                sortType === opt.value
                                    ? "bg-purple-500 text-white"
                                    : "bg-gray-100 text-gray-600 hover:bg-purple-500 hover:text-white"
                            }`}
                        >
                            {opt.label}
                        </button>
                    ))}
                </div>
            </div>

            <section className="grid grid-cols-[1fr_280px] gap-7">
            {/* TIL 목록 */}
            <div className="rounded-3xl bg-white p-8 shadow-sm">
                <h3 className="mb-6 text-xl font-bold">
                    내 TIL {pageInfo.totalElements}개
                </h3>

                {posts.length === 0 ? (
                    <div className="py-20 text-center">
                        <p className="text-lg font-bold text-gray-400">검색 결과가 없습니다</p>
                        <p className="mt-2 text-sm text-gray-300">다른 검색어나 필터를 사용해보세요</p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {posts.map((post) => (
                            <div
                                key={post.postId}
                                onClick={() => handleMoveDetail(post.postId)}
                                className={`cursor-pointer rounded-2xl border border-gray-100 border-l-4 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md ${
                                    difficultyBorderStyle[post.difficulty] || difficultyBorderStyle.NORMAL
                                }`}
                            >
                                <div className="flex items-center justify-between gap-5">
                                    <div className="min-w-0">
                                        <div className="mb-2 flex flex-wrap gap-2">
                                            {(post.tagNames || []).map((tag) => (
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

                                        <h4 className="text-lg font-bold">{post.title}</h4>

                                        <p className="mt-2 text-sm text-gray-500">
                                            난이도 {post.difficulty}
                                            · 조회수 {post.viewCount}회
                                            · 학습시간 {post.studyTime}분
                                            · 좋아요 {post.likeCount}개
                                            · 댓글 {post.commentCount}개
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
                )}

                {/* 페이지네이션 */}
                {totalPages > 0 && (
                    <div className="mt-16 flex items-center justify-center gap-2 font-bold">
                        <button
                            type="button"
                            disabled={pageInfo.first}
                            onClick={() => setPage(currentPage - 1)}
                            className="rounded-full bg-gray-100 px-4 py-2 text-sm text-gray-600 disabled:cursor-not-allowed disabled:opacity-40"
                        >
                            이전
                        </button>

                        {pageNumbers.map((pn) => (
                            <button
                                key={pn}
                                type="button"
                                onClick={() => setPage(pn)}
                                className={`h-10 w-10 rounded-full text-sm ${
                                    currentPage === pn
                                        ? "bg-purple-500 text-white"
                                        : "bg-gray-100 text-gray-600"
                                }`}
                            >
                                {pn + 1}
                            </button>
                        ))}

                        <button
                            type="button"
                            disabled={pageInfo.last}
                            onClick={() => setPage(currentPage + 1)}
                            className="rounded-full bg-gray-100 px-4 py-2 text-sm text-gray-600 disabled:cursor-not-allowed disabled:opacity-40"
                        >
                            다음
                        </button>
                    </div>
                )}
            </div>

            {/* 우측 보조 정보 */}
            <aside className="rounded-3xl bg-white p-7 shadow-sm">
                <h3 className="text-xl font-bold">보조 정보</h3>

                <div className="mt-6 space-y-4">
                    <div className="rounded-2xl border border-gray-100 p-5">
                        <p className="text-sm font-bold text-gray-500">현재 조회 TIL</p>
                        <p className="mt-2 text-3xl font-bold">
                            {pageInfo.totalElements}개
                        </p>
                    </div>

                    <div className="rounded-2xl border border-gray-100 p-5">
                        <p className="text-sm font-bold text-gray-500">평균 학습시간</p>
                        <p className="mt-2 text-3xl font-bold">
                            {posts.length > 0
                                ? Math.round(
                                      posts.reduce((sum, p) => sum + (p.studyTime || 0), 0) /
                                          posts.length
                                  )
                                : 0}
                            <span className="text-base">분</span>
                        </p>
                    </div>
                </div>
            </aside>
            </section>
        </main>
    );
}

export default MyTilListPage;
