import {
    useEffect,
    useMemo,
    useState,
} from 'react';
import { useNavigate } from 'react-router-dom';
import {
    getCachedHeatmap,
    getCachedStreak,
    getMyHeatmap,
    getMyStreak,
} from '../api/myPageApi';
import {
    getCachedRecentTils,
    getRecentTils,
} from '../api/tilApi';
import {
    cancelSubscription,
    getCurrentPaybackParticipation,
    getMySubscriptionStatus,
    resumeSubscription,
    subscribePremium,
} from '../api/subscriptionPaybackApi';
import MyPageHero from '../components/mypage/MyPageHero';
import MyPageStats from '../components/mypage/MyPageStats';
import HeatmapSection from '../components/mypage/HeatmapSection';
import RecentTilSection from '../components/mypage/RecentTilSection';
import SubscriptionPaybackSection from '../components/mypage/SubscriptionPaybackSection';
import {
    buildHeatmapDays,
    getMonthRange,
    normalizeHeatmapItems,
    normalizeTilList,
} from '../utils/mypageUtils';
import { getMemberId, isLoggedIn } from '../utils/authUtils';

const DEFAULT_STREAK = {
    currentStreak: 0,
    longestStreak: 0,
    totalTilCount: 0,
    totalWrittenDays: 0,
};

const normalizeStreak = (streakResponse) => {
    if (!streakResponse) {
        return DEFAULT_STREAK;
    }

    return {
        currentStreak: streakResponse.currentStreak ?? 0,
        longestStreak: streakResponse.longestStreak ?? 0,
        totalTilCount:
            streakResponse.totalTilCount ??
            streakResponse.totalWrittenCount ??
            streakResponse.totalWriteCount ??
            0,
        totalWrittenDays:
            streakResponse.totalWrittenDays ??
            streakResponse.writtenDays ??
            0,
    };
};

const getInitialHeatmapItems = (monthCount, memberId) => {
    const { startDate, endDate } = getMonthRange(monthCount);

    const cachedHeatmap = getCachedHeatmap({
        memberId,
        startDate,
        endDate,
    });

    return normalizeHeatmapItems(cachedHeatmap);
};

const getInitialStreak = (memberId) => {
    return normalizeStreak(getCachedStreak(memberId));
};

const getInitialRecentTils = () => {
    return normalizeTilList(
        getCachedRecentTils({
            page: 0,
            size: 4,
            sort: 'LATEST',
        })
    );
};

const MyPage = () => {
    const navigate = useNavigate();
    const memberId = getMemberId();

    const [selectedMonthCount, setSelectedMonthCount] = useState(6);

    const [streak, setStreak] = useState(() => getInitialStreak(memberId));
    const [heatmapItems, setHeatmapItems] = useState(() => getInitialHeatmapItems(6, memberId));
    const [recentTils, setRecentTils] = useState(() => getInitialRecentTils());

    const [subscription, setSubscription] = useState(null);
    const [payback, setPayback] = useState(null);

    const [isStreakLoading, setIsStreakLoading] = useState(() => {
        return getCachedStreak(memberId) === null;
    });

    const [isHeatmapLoading, setIsHeatmapLoading] = useState(() => {
        return getInitialHeatmapItems(6, memberId).length === 0;
    });

    const [isTilLoading, setIsTilLoading] = useState(() => {
        return getInitialRecentTils().length === 0;
    });

    const [isSubscriptionLoading, setIsSubscriptionLoading] = useState(true);
    const [isSubscriptionActionLoading, setIsSubscriptionActionLoading] = useState(false);

    const heatmapDays = useMemo(
        () => buildHeatmapDays(heatmapItems, selectedMonthCount),
        [heatmapItems, selectedMonthCount]
    );

    const totalWriteCount = heatmapDays.reduce((sum, day) => sum + day.writeCount, 0);
    const writtenDays = heatmapDays.filter((day) => day.writeCount > 0).length;

    const fetchSubscriptionAndPayback = async () => {
        try {
            setIsSubscriptionLoading(true);

            const subscriptionResponse = await getMySubscriptionStatus();
            setSubscription(subscriptionResponse);

            // ACTIVE 또는 CANCEL_RESERVED 모두 페이백 조회
            const hasValidSubscription =
                subscriptionResponse?.isActive ||
                subscriptionResponse?.status === 'CANCEL_RESERVED';

            if (hasValidSubscription) {
                try {
                    const paybackResponse = await getCurrentPaybackParticipation();
                    setPayback(paybackResponse);
                } catch (error) {
                    console.error('[PAYBACK API ERROR]', error);
                    setPayback(null);
                }
            } else {
                setPayback(null);
            }
        } catch (error) {
            console.error('[SUBSCRIPTION API ERROR]', error);
            setSubscription(null);
            setPayback(null);
        } finally {
            setIsSubscriptionLoading(false);
        }
    };

    const handleSubscribe = async () => {
        try {
            setIsSubscriptionActionLoading(true);
            await subscribePremium();
            await fetchSubscriptionAndPayback();
        } catch (error) {
            console.error('[SUBSCRIBE API ERROR]', error);
            alert(error.message ?? '구독 신청에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
        }
    };

    const handleCancelSubscription = async () => {
        const isConfirmed = window.confirm('구독을 취소하시겠습니까?');

        if (!isConfirmed) {
            return;
        }

        try {
            setIsSubscriptionActionLoading(true);
            await cancelSubscription();
            await fetchSubscriptionAndPayback();
        } catch (error) {
            console.error('[SUBSCRIPTION CANCEL API ERROR]', error);
            alert(error.message ?? '구독 취소에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
        }
    };

    const handleResumeSubscription = async () => {
        try {
            setIsSubscriptionActionLoading(true);
            await resumeSubscription();
            await fetchSubscriptionAndPayback();
        } catch (error) {
            console.error('[SUBSCRIPTION RESUME API ERROR]', error);
            alert(error.message ?? '구독 재개에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
        }
    };

    useEffect(() => {
        const fetchStreak = async () => {
            const cachedStreak = getCachedStreak(memberId);

            if (cachedStreak) {
                setStreak(normalizeStreak(cachedStreak));
                setIsStreakLoading(false);
                return;
            }

            try {
                setIsStreakLoading(true);

                const streakResponse = await getMyStreak({
                    memberId: memberId,
                    useCache: true,
                });

                setStreak(normalizeStreak(streakResponse));
            } catch (error) {
                console.error('[STREAK API ERROR]', error);
                setStreak(DEFAULT_STREAK);
            } finally {
                setIsStreakLoading(false);
            }
        };

        fetchStreak();
    }, []);

    useEffect(() => {
        const fetchHeatmap = async () => {
            const { startDate, endDate } = getMonthRange(selectedMonthCount);

            const cachedHeatmap = getCachedHeatmap({
                memberId: memberId,
                startDate,
                endDate,
            });

            if (cachedHeatmap) {
                setHeatmapItems(normalizeHeatmapItems(cachedHeatmap));
                setIsHeatmapLoading(false);
                return;
            }

            try {
                setIsHeatmapLoading(true);

                const heatmapResponse = await getMyHeatmap({
                    memberId: memberId,
                    startDate,
                    endDate,
                    useCache: true,
                });

                setHeatmapItems(normalizeHeatmapItems(heatmapResponse));
            } catch (error) {
                console.error('[HEATMAP API ERROR]', error);
                setHeatmapItems([]);
            } finally {
                setIsHeatmapLoading(false);
            }
        };

        fetchHeatmap();
    }, [selectedMonthCount]);

    useEffect(() => {
        const fetchRecentTils = async () => {
            const cachedRecentTils = getCachedRecentTils({
                page: 0,
                size: 4,
                sort: 'LATEST',
            });

            if (cachedRecentTils) {
                setRecentTils(normalizeTilList(cachedRecentTils));
                setIsTilLoading(false);
                return;
            }

            try {
                setIsTilLoading(true);

                const tilResponse = await getRecentTils({
                    page: 0,
                    size: 4,
                    sort: 'LATEST',
                    useCache: true,
                });

                setRecentTils(normalizeTilList(tilResponse));
            } catch (error) {
                console.error('[TIL API ERROR]', error);
                setRecentTils([]);
            } finally {
                setIsTilLoading(false);
            }
        };

        fetchRecentTils();
    }, []);

    useEffect(() => {
        if (!isLoggedIn()) {
            navigate('/login', { replace: true });
            return;
        }
        fetchSubscriptionAndPayback();
    }, [navigate]);

    return (
        <>
            <MyPageHero />

            <MyPageStats
                streak={streak}
                totalWriteCount={totalWriteCount}
                writtenDays={writtenDays}
                isLoading={isStreakLoading}
            />

            <section className="mypage-content-grid">
                <HeatmapSection
                    heatmapDays={heatmapDays}
                    selectedMonthCount={selectedMonthCount}
                    onChangeMonthCount={setSelectedMonthCount}
                    isLoading={isHeatmapLoading}
                />

                <div className="mypage-side-column">
                    <SubscriptionPaybackSection
                        subscription={subscription}
                        payback={payback}
                        isLoading={isSubscriptionLoading}
                        isActionLoading={isSubscriptionActionLoading}
                        onSubscribe={handleSubscribe}
                        onCancel={handleCancelSubscription}
                        onResume={handleResumeSubscription}
                    />

                    <RecentTilSection
                        recentTils={recentTils}
                        isLoading={isTilLoading}
                    />
                </div>
            </section>
        </>
    );
};

export default MyPage;
