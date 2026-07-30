import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getMemberTils } from "../../api/feed";
import { difficultyStyle, difficultyBorderStyle } from "../../constants/post";
import { getPostDetailPath } from "../../constants/post";

const PAGE_SIZE = 10;

const difficultyLabel = {
    EASY: "쉬움",
    NORMAL: "보통",
    HARD: "어려움",
};

function MemberTilListPage() {
    const { memberId } = useParams();
    const navigate = useNavigate();

    const [tils, setTils] = useState([]);
    const [nickname, setNickname] = useState("");
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setPage(0);
        setTils([]);
        setHasMore(true);
    }, [memberId]);

    useEffect(() => {
        const fetchTils = async () => {
            setLoading(true);
            try {
                const data = await getMemberTils(memberId, page, PAGE_SIZE);
                if (page === 0) {
                    setTils(data);
                    if (data.length > 0) setNickname(data[0].nickname);
                } else {
                    setTils((prev) => [...prev, ...data]);
                }
                setHasMore(data.length === PAGE_SIZE);
            } catch {
                setHasMore(false);
            } finally {
                setLoading(false);
            }
        };

        fetchTils();
    }, [memberId, page]);

    const handleLoadMore = () => setPage((prev) => prev + 1);

    return (
        <main className="space-y-7">
            {/* 헤더 배너 */}
            <section className="rounded-3xl border-2 border-cyan-400 bg-gradient-to-r from-purple-50 to-cyan-50 p-8">
                <div className="flex items-center gap-4">
                    {/* 아바타 */}
                    <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-purple-500 to-cyan-400 text-2xl font-bold text-white shadow-md">
                        {nickname?.charAt(0).toUpperCase() || "?"}
                    </div>
                    <div>
                        <h2 className="text-2xl font-bold text-slate-900">
                            {nickname ? `${nickname}님의 TIL` : "TIL 목록"}
                        </h2>
                        <p className="mt-1 text-sm text-gray-500">
                            공개된 학습 기록을 모아봤어요
                        </p>
                    </div>
                    <button
                        type="button"
                        onClick={() => navigate(-1)}
                        className="ml-auto rounded-xl border border-gray-200 bg-white px-5 py-2 text-sm font-bold text-gray-500 transition hover:bg-gray-50"
                    >
                        ← 돌아가기
                    </button>
                </div>
            </section>

            {/* TIL 목록 */}
            <section className="rounded-3xl bg-white p-8 shadow-sm">
                {loading && tils.length === 0 ? (
                    <div className="py-20 text-center text-gray-400">불러오는 중...</div>
                ) : tils.length === 0 ? (
                    <div className="py-20 text-center">
                        <p className="text-lg font-bold text-gray-400">아직 작성된 TIL이 없습니다</p>
                        <p className="mt-2 text-sm text-gray-300">이 회원은 아직 공개 TIL을 작성하지 않았어요</p>
                    </div>
                ) : (
                    <>
                        <p className="mb-6 text-sm font-semibold text-gray-400">
                            총 {tils.length}개{hasMore ? " 이상" : ""}의 TIL
                        </p>

                        <div className="space-y-4">
                            {tils.map((til) => (
                                <div
                                    key={til.postId}
                                    onClick={() => navigate(getPostDetailPath(til.postId))}
                                    className={`cursor-pointer rounded-2xl border border-gray-100 border-l-4 bg-white p-5 shadow-sm transition hover:shadow-md hover:-translate-y-0.5 ${
                                        difficultyBorderStyle?.[til.difficulty] ?? "border-l-gray-300"
                                    }`}
                                >
                                    <div className="flex items-start justify-between gap-4">
                                        <div className="min-w-0 flex-1">
                                            {/* 제목 */}
                                            <h3 className="truncate text-base font-bold text-slate-800">
                                                {til.title}
                                            </h3>

                                            {/* 메타 */}
                                            <p className="mt-2 text-xs text-gray-400">
                                                {til.createdAt
                                                    ? new Date(til.createdAt).toLocaleDateString("ko-KR")
                                                    : ""}
                                            </p>
                                        </div>

                                        {/* 난이도 뱃지 */}
                                        <span
                                            className={`shrink-0 rounded-full px-4 py-1.5 text-xs font-bold ${
                                                difficultyStyle?.[til.difficulty] ?? "bg-gray-100 text-gray-500"
                                            }`}
                                        >
                                            {difficultyLabel[til.difficulty] ?? til.difficulty}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* 더 보기 버튼 */}
                        {hasMore && (
                            <div className="mt-8 text-center">
                                <button
                                    type="button"
                                    onClick={handleLoadMore}
                                    disabled={loading}
                                    className="rounded-2xl bg-gradient-to-r from-purple-500 to-cyan-400 px-10 py-3 font-bold text-white transition hover:opacity-90 disabled:opacity-50"
                                >
                                    {loading ? "불러오는 중..." : "더 보기"}
                                </button>
                            </div>
                        )}
                    </>
                )}
            </section>
        </main>
    );
}

export default MemberTilListPage;
