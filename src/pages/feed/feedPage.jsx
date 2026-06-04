import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getMyHeatmap, getMyStreak } from "../../api/myPageApi";
import { getPopularTags, searchPosts } from "../../api/post";
import { difficultyStyle } from "../../constants/post";
import {
    DEFAULT_GROWTH_SUMMARY,
    getCurrentStreak,
    getMonthWriteCount,
} from "../../utils/growthStats";
import {
    getMonthRange,
    TEMP_MEMBER_ID,
} from "../../utils/mypageUtils";

// 메인 피드 페이지
function FeedPage() {
    const navigate = useNavigate();

    const [feedPosts, setFeedPosts] = useState([]);
    const [popularTags, setPopularTags] = useState([]);
    const [growthSummary, setGrowthSummary] = useState(DEFAULT_GROWTH_SUMMARY);
    const [isGrowthSummaryLoading, setIsGrowthSummaryLoading] = useState(true);

    useEffect(() => {
        const fetchFeedPosts = async () => {
            const posts = await searchPosts({ sort: "LATEST", size: 5 });
            setFeedPosts(posts);
        };

        fetchFeedPosts();
    }, []);

    useEffect(() => {
        const fetchPopularTags = async () => {
            try {
                const tags = await getPopularTags({ limit: 4 });
                setPopularTags(tags);
            } catch (error) {
                console.error(error);
                setPopularTags([]);
            }
        };

        fetchPopularTags();
    }, []);

    useEffect(() => {
        const fetchGrowthSummary = async () => {
            const { startDate, endDate } = getMonthRange(1);

            try {
                setIsGrowthSummaryLoading(true);

                const [streakResponse, heatmapResponse] = await Promise.all([
                    getMyStreak({
                        memberId: TEMP_MEMBER_ID,
                        useCache: true,
                    }),
                    getMyHeatmap({
                        memberId: TEMP_MEMBER_ID,
                        startDate,
                        endDate,
                        useCache: true,
                    }),
                ]);

                setGrowthSummary({
                    currentStreak: getCurrentStreak(streakResponse),
                    monthWriteCount: getMonthWriteCount(heatmapResponse),
                });
            } catch (error) {
                console.error("[GROWTH SUMMARY API ERROR]", error);
                setGrowthSummary(DEFAULT_GROWTH_SUMMARY);
            } finally {
                setIsGrowthSummaryLoading(false);
            }
        };

        fetchGrowthSummary();
    }, []);

    return (
        <main className="space-y-7">
            {/* 상단 배너 */}
            <section className="rounded-3xl bg-gradient-to-r from-purple-500 to-cyan-400 p-8 text-white">
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-3xl font-bold">
                            오늘의 개발 기록을 둘러보세요
                        </h2>

                        <p className="mt-3 text-sm font-semibold text-white/90">
                            다른 개발자의 TIL을 읽고 내 학습 흐름도 함께 쌓아보세요.
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={() => navigate("/posts/write")}
                        className="rounded-xl bg-white/20 px-8 py-3 font-bold text-white backdrop-blur transition hover:bg-white/30"
                    >
                        TIL 작성하기
                    </button>
                </div>
            </section>

            {/* 본문 3단 영역 */}
            <section className="grid grid-cols-[230px_1fr_260px] gap-6">
                {/* 왼쪽 성장 요약 */}
                <aside className="rounded-3xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h3 className="text-xl font-bold">
                        내 성장 요약
                    </h3>

                    <div className="mt-6 space-y-4">
                        <div className="rounded-2xl border border-gray-100 p-5">
                            <div className="flex items-center gap-4">
                                <div className="flex h-14 w-14 items-center justify-center rounded-full bg-orange-50 text-3xl">
                                    🔥
                                </div>

                                <div>
                                    <p className="text-sm font-bold text-gray-500">
                                        현재 스트릭
                                    </p>
                                    <p className="text-3xl font-bold">
                                        {isGrowthSummaryLoading ? "..." : growthSummary.currentStreak}
                                        <span className="text-base">일</span>
                                    </p>
                                </div>
                            </div>

                            <p className="mt-3 text-xs text-gray-400">
                                연속 작성 중
                            </p>
                        </div>

                        <div className="rounded-2xl border border-gray-100 p-5">
                            <div className="flex items-center gap-4">
                                <div className="flex h-14 w-14 items-center justify-center rounded-full bg-green-50 text-3xl">
                                    🌱
                                </div>

                                <div>
                                    <p className="text-sm font-bold text-gray-500">
                                        이번 달 작성
                                    </p>
                                    <p className="text-3xl font-bold">
                                        {isGrowthSummaryLoading ? "..." : growthSummary.monthWriteCount}
                                        <span className="text-base">개</span>
                                    </p>
                                </div>
                            </div>

                            <p className="mt-3 text-xs text-gray-400">
                                꾸준히 증가 중
                            </p>
                        </div>
                    </div>

                    {/* 인기 태그 */}
                    <div className="mt-7">
                        <h4 className="font-bold">
                            인기 태그
                        </h4>

                        <div className="mt-4 flex flex-wrap gap-2">
                            {popularTags.length > 0 ? (
                                popularTags.map((tag) => (
                                    <button
                                        key={tag.tagName}
                                        type="button"
                                        onClick={() => navigate(`/posts?tagName=${encodeURIComponent(tag.tagName)}`)}
                                        className="rounded-full bg-gradient-to-r from-purple-500 to-cyan-400 px-4 py-2 text-xs font-bold text-white"
                                    >
                                        #{tag.tagName}
                                        {tag.count > 0 && (
                                            <span className="ml-1 text-white/80">
                                                {tag.count}
                                            </span>
                                        )}
                                    </button>
                                ))
                            ) : (
                                <p className="text-sm font-semibold text-gray-400">
                                    표시할 인기 태그가 없습니다.
                                </p>
                            )}
                        </div>
                    </div>
                </aside>

                {/* 가운데 최신 피드 */}
                <section className="rounded-3xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h3 className="text-xl font-bold">
                        최신 TIL 피드
                    </h3>

                    <div className="mt-6 space-y-4">
                        {feedPosts.map((post) => (
                            <button
                                key={post.postId}
                                type="button"
                                onClick={() => navigate(`/posts/${post.postId}`)}
                                className="flex w-full items-center justify-between rounded-2xl border border-gray-100 bg-white px-5 py-4 text-left transition hover:shadow-md"
                            >
                                <div className="flex min-w-0 items-center gap-4">
                                    <span className={`shrink-0 rounded-full px-5 py-2 text-xs font-bold ${
                                            difficultyStyle[post.difficulty]
                                        }`}
                                    >
                                        {post.difficulty}
                                    </span>

                                    <div className="min-w-0">
                                        <h4 className="truncate font-bold">
                                            {post.title}
                                        </h4>

                                        <p className="mt-1 text-xs text-gray-500">
                                            {post.nickname}
                                            · 조회수 {post.viewCount}회
                                            · 학습시간 {post.studyTime}분
                                            · 댓글 {post.commentCount}개
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-center gap-4 text-xs font-semibold text-gray-400">
                                    <span>
                                        {new Date(post.createdAt).toLocaleDateString("ko-KR", {
                                            month: "2-digit",
                                            day: "2-digit",
                                        })}
                                    </span>
                                    <span>›</span>
                                </div>
                            </button>
                        ))}
                    </div>
                </section>

                {/* 오른쪽 챌린지 */}
                <aside className="rounded-3xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h3 className="text-xl font-bold">
                        오늘의 챌린지
                    </h3>

                    <div className="mt-5">
                        <p className="font-bold text-purple-600">
                            6월 TIL 챌린지
                        </p>

                        <p className="mt-2 text-sm text-gray-500">
                            목표 20일 중 12일 달성
                        </p>

                        <div className="mt-5 h-3 rounded-full bg-gray-200">
                            <div className="h-3 w-3/5 rounded-full bg-gradient-to-r from-purple-500 to-cyan-400" />
                        </div>
                    </div>

                    <div className="mt-8">
                        <h4 className="font-bold">
                            추천 학습 주제
                        </h4>

                        <ul className="mt-4 space-y-4 text-sm font-semibold text-gray-700">
                            <li># JWT 인증 흐름</li>
                            <li># JPA 연관관계</li>
                            <li># React Hook</li>
                            <li># 쿼리 최적화</li>
                        </ul>
                    </div>
                </aside>
            </section>
        </main>
    );
}

export default FeedPage;
