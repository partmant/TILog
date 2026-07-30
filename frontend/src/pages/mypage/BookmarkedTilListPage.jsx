import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useBookmarkedTilList } from "../../hooks/mypage/useBookmarkedTilList";
import { useBookmark } from "../../hooks/post/useBookmark";
import { useToast } from "../../hooks/useToast";
import { difficultyStyle, difficultyBorderStyle } from "../../constants/post";
import Toast from "../../components/common/Toast.jsx";

const DIFFICULTY_LABEL = { EASY: "쉬움", NORMAL: "보통", HARD: "어려움" };

// 즐겨찾기 별 아이콘
const BookmarkIcon = ({ isBookmarked, onClick, disabled }) => (
    <button
        type="button"
        onClick={onClick}
        disabled={disabled}
        aria-label={isBookmarked ? "즐겨찾기 해제" : "즐겨찾기 등록"}
        style={{
            background: "none",
            border: "none",
            cursor: disabled ? "not-allowed" : "pointer",
            padding: "4px",
            borderRadius: "50%",
            display: "flex",
            alignItems: "center",
            opacity: disabled ? 0.5 : 1,
            flexShrink: 0,
        }}
    >
        {isBookmarked ? (
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="26" height="26" fill="#f59e0b">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
        ) : (
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="#9ca3af" strokeWidth="1.8">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
        )}
    </button>
);

function BookmarkedTilListPage() {
    const navigate = useNavigate();

    const {
        posts,
        pageInfo,
        currentPage,
        keyword,
        selectedDifficulty,
        sortType,
        isLoading,
        error,
        setPage,
        setSelectedDifficulty,
        setSortType,
        setKeyword,
        handleBookmarkChange,
    } = useBookmarkedTilList();

    const { toast, showToast } = useToast();
    const { handleToggleBookmark, loadingPostId } = useBookmark(handleBookmarkChange, showToast);

    // 검색 입력 로컬 상태 (Enter/버튼 눌렀을 때만 URL 반영)
    const [searchInput, setSearchInput] = useState(keyword);
    useEffect(() => { setSearchInput(keyword); }, [keyword]);

    const handleSearch = () => setKeyword(searchInput.trim());

    useEffect(() => {
        window.scrollTo({ top: 0, behavior: "instant" });
    }, [currentPage]);

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
        <>
        <Toast toast={toast} />
        <main className="space-y-7">
            {/* 상단 헤더 배너 */}
            <section className="rounded-3xl border-2 border-amber-300 bg-gradient-to-r from-amber-50 to-yellow-50 p-8">
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-3xl font-bold">즐겨찾기한 TIL</h2>
                        <p className="mt-2 text-gray-600">
                            나중에 다시 보고 싶어 즐겨찾기한 TIL 목록입니다.
                        </p>
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

            {/* 검색바 */}
            <div className="flex justify-end">
                <div className="flex items-center gap-2">
                    <input
                        type="text"
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                        placeholder="제목 키워드 검색"
                        className="w-64 rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm outline-none transition focus:border-purple-400 focus:ring-2 focus:ring-purple-100"
                    />
                    <button
                        type="button"
                        onClick={handleSearch}
                        className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-5 py-2.5 text-sm font-bold text-white transition hover:opacity-90"
                    >
                        검색
                    </button>
                </div>
            </div>

            {/* 난이도 필터(좌) + 정렬(우) */}
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
                        { value: "LIKES",    label: "좋아요순" },
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

            <section className="grid grid-cols-[1fr_280px] items-start gap-7">
                {/* 즐겨찾기 목록 */}
                <div className="rounded-3xl bg-white p-8 shadow-sm">
                    <h3 className="mb-6 text-xl font-bold">
                        즐겨찾기 {pageInfo.totalElements}개
                    </h3>

                    {isLoading ? (
                        <div className="py-20 text-center text-gray-400 font-bold">
                            즐겨찾기 목록을 불러오는 중입니다...
                        </div>
                    ) : error ? (
                        <div className="py-20 text-center text-red-400 font-bold">{error}</div>
                    ) : posts.length === 0 ? (
                        <div className="py-20 text-center">
                            <p className="text-lg font-bold text-gray-400">
                                {keyword || selectedDifficulty !== "ALL"
                                    ? "검색 결과가 없습니다."
                                    : "즐겨찾기한 TIL이 없습니다."}
                            </p>
                            <p className="mt-2 text-sm text-gray-300">
                                {keyword || selectedDifficulty !== "ALL"
                                    ? "다른 검색어나 필터를 사용해보세요."
                                    : "TIL 목록에서 별 아이콘을 눌러 즐겨찾기해보세요."}
                            </p>
                            <button
                                type="button"
                                onClick={() => navigate("/posts")}
                                className="mt-6 rounded-full bg-purple-500 px-8 py-2 text-sm font-bold text-white hover:bg-purple-600"
                            >
                                TIL 목록 보기
                            </button>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {posts.map((post) => (
                                <div
                                    key={post.postId}
                                    onClick={() => navigate(`/posts/${post.postId}`)}
                                    className={`cursor-pointer rounded-2xl border border-gray-100 border-l-4 bg-white p-5 shadow-sm transition hover:shadow-md ${
                                        difficultyBorderStyle[post.difficulty] || difficultyBorderStyle.NORMAL
                                    }`}
                                >
                                    {/* 태그 행: 태그(좌) + 즐겨찾기 별(우) */}
                                    <div className="mb-2 flex min-h-[28px] items-center justify-between gap-2">
                                        <div className="flex flex-wrap gap-2">
                                            {(post.tagNames || []).map((tag) => (
                                                <span
                                                    key={tag}
                                                    className="rounded-full bg-purple-100 px-3 py-1 text-xs font-bold text-purple-600"
                                                >
                                                    #{tag}
                                                </span>
                                            ))}
                                        </div>
                                        <BookmarkIcon
                                            isBookmarked={post.isBookmarked ?? true}
                                            disabled={loadingPostId === post.postId}
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleToggleBookmark(
                                                    post.postId,
                                                    post.isBookmarked ?? true
                                                );
                                            }}
                                        />
                                    </div>

                                    {/* 제목 + 메타 + 난이도 배지 */}
                                    <div className="flex items-start justify-between gap-4">
                                        <div className="min-w-0">
                                            <h4 className="text-lg font-bold">{post.title}</h4>
                                            <p className="mt-2 text-sm text-gray-500">
                                                작성자 {post.nickname}
                                                · 난이도 {DIFFICULTY_LABEL[post.difficulty] ?? post.difficulty}
                                                · 학습시간 {post.studyTime ?? 0}분
                                                · 좋아요 {post.likeCount ?? 0}개
                                                · 댓글 {post.commentCount ?? 0}개
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
                    {totalPages > 1 && (
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
                <aside className="sticky top-4 self-start rounded-3xl bg-white p-7 shadow-sm">
                    <h3 className="text-xl font-bold">즐겨찾기 현황</h3>

                    <div className="mt-6 space-y-4">
                        <div className="rounded-2xl border border-gray-100 p-5">
                            <p className="text-sm font-bold text-gray-500">총 즐겨찾기</p>
                            <p className="mt-2 text-3xl font-bold">
                                {pageInfo.totalElements}
                                <span className="text-base">개</span>
                            </p>
                        </div>

                        <div className="rounded-2xl border border-gray-100 p-5">
                            <p className="text-sm font-bold text-gray-500">평균 학습시간</p>
                            <p className="mt-2 text-3xl font-bold">
                                {posts.length > 0
                                    ? Math.round(
                                          posts.reduce(
                                              (sum, p) => sum + (p.studyTime || 0),
                                              0
                                          ) / posts.length
                                      )
                                    : 0}
                                <span className="text-base">분</span>
                            </p>
                        </div>

                        <div
                            className="rounded-2xl border border-amber-100 bg-amber-50 p-5 cursor-pointer hover:bg-amber-100 transition"
                            onClick={() => navigate("/posts")}
                        >
                            <p className="text-sm font-bold text-amber-600">TIL 목록으로</p>
                            <p className="mt-1 text-xs text-amber-500">
                                새로운 TIL을 즐겨찾기해보세요
                            </p>
                        </div>
                    </div>
                </aside>
            </section>
        </main>
        </>
    );
}

export default BookmarkedTilListPage;
