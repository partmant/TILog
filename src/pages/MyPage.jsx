import {
    useCallback,
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
    cancelSubscription,
    getCurrentPaybackParticipation,
    getMySubscriptionStatus,
    resumeSubscription,
    subscribePremium,
} from '../api/subscriptionPaybackApi';
import { getMemberTils } from '../api/feed';
import MyPageHero from '../components/mypage/MyPageHero';
import MyPageStats from '../components/mypage/MyPageStats';
import HeatmapSection from '../components/mypage/HeatmapSection';
import RecentTilSection from '../components/mypage/RecentTilSection';
import SubscriptionPaybackSection from '../components/mypage/SubscriptionPaybackSection';
import WeeklyReportSection from '../components/mypage/WeeklyReportSection';

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

const RECENT_TIL_CACHE_TTL = 1000 * 60 * 5;

const getRecentTilCacheKey = (memberId) => {
    return `mypage:recent-tils:member:${memberId}:page:0:size:4`;
};

const getCachedMyRecentTils = (memberId) => {
    if (!memberId) {
        return null;
    }

    const cached = localStorage.getItem(getRecentTilCacheKey(memberId));

    if (!cached) {
        return null;
    }

    try {
        const parsed = JSON.parse(cached);

        if (Date.now() - parsed.cachedAt > RECENT_TIL_CACHE_TTL) {
            localStorage.removeItem(getRecentTilCacheKey(memberId));
            return null;
        }

        return parsed.data;
    } catch {
        localStorage.removeItem(getRecentTilCacheKey(memberId));
        return null;
    }
};

const setCachedMyRecentTils = (memberId, data) => {
    if (!memberId) {
        return;
    }

    localStorage.setItem(
        getRecentTilCacheKey(memberId),
        JSON.stringify({
            cachedAt: Date.now(),
            data,
        })
    );
};

const getInitialRecentTils = (memberId) => {
    return normalizeTilList(getCachedMyRecentTils(memberId));
};

const MyPage = () => {
    const navigate = useNavigate();
    const memberId = getMemberId();

    const [selectedMonthCount, setSelectedMonthCount] = useState(6);

    const [streak, setStreak] = useState(() => getInitialStreak(memberId));
    const [heatmapItems, setHeatmapItems] = useState(() => getInitialHeatmapItems(6, memberId));
    const [recentTils, setRecentTils] = useState(() => getInitialRecentTils(memberId));

    const [subscription, setSubscription] = useState(null);
    const [payback, setPayback] = useState(null);

    const [isStreakLoading, setIsStreakLoading] = useState(() => {
        return getCachedStreak(memberId) === null;
    });

    const [isHeatmapLoading, setIsHeatmapLoading] = useState(() => {
        return getInitialHeatmapItems(6, memberId).length === 0;
    });

    const [isTilLoading, setIsTilLoading] = useState(() => {
        return getInitialRecentTils(memberId).length === 0;
    });

    const [isSubscriptionLoading, setIsSubscriptionLoading] = useState(true);
    const [isSubscriptionActionLoading, setIsSubscriptionActionLoading] = useState(false);

    const heatmapDays = useMemo(
        () => buildHeatmapDays(heatmapItems, selectedMonthCount),
        [heatmapItems, selectedMonthCount]
    );

    const totalWriteCount = heatmapDays.reduce((sum, day) => sum + day.writeCount, 0);
    const writtenDays = heatmapDays.filter((day) => day.writeCount > 0).length;

    const loadSubscriptionAndPayback = useCallback(async () => {
        const subscriptionResponse = await getMySubscriptionStatus();

        const hasValidSubscription =
            subscriptionResponse?.isActive ||
            subscriptionResponse?.status === 'CANCEL_RESERVED';

        if (!hasValidSubscription) {
            return {
                subscriptionResponse,
                paybackResponse: null,
            };
        }

        try {
            const paybackResponse = await getCurrentPaybackParticipation();

            return {
                subscriptionResponse,
                paybackResponse,
            };
        } catch (error) {
            console.error('[PAYBACK API ERROR]', error);

            return {
                subscriptionResponse,
                paybackResponse: null,
            };
        }
    }, []);

    const applySubscriptionAndPayback = ({
                                             subscriptionResponse,
                                             paybackResponse,
                                         }) => {
        setSubscription(subscriptionResponse);
        setPayback(paybackResponse);
    };

    const handleSubscribe = async () => {
        try {
            setIsSubscriptionActionLoading(true);
            setIsSubscriptionLoading(true);

            await subscribePremium();

            const result = await loadSubscriptionAndPayback();
            applySubscriptionAndPayback(result);
        } catch (error) {
            console.error('[SUBSCRIBE API ERROR]', error);
            alert(error.message ?? '구독 신청에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
            setIsSubscriptionLoading(false);
        }
    };

    const handleCancelSubscription = async () => {
        const isConfirmed = window.confirm('구독을 취소하시겠습니까?');

        if (!isConfirmed) {
            return;
        }

        try {
            setIsSubscriptionActionLoading(true);
            setIsSubscriptionLoading(true);

            await cancelSubscription();

            const result = await loadSubscriptionAndPayback();
            applySubscriptionAndPayback(result);
        } catch (error) {
            console.error('[SUBSCRIPTION CANCEL API ERROR]', error);
            alert(error.message ?? '구독 취소에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
            setIsSubscriptionLoading(false);
        }
    };

    const handleResumeSubscription = async () => {
        try {
            setIsSubscriptionActionLoading(true);
            setIsSubscriptionLoading(true);

            await resumeSubscription();

            const result = await loadSubscriptionAndPayback();
            applySubscriptionAndPayback(result);
        } catch (error) {
            console.error('[SUBSCRIPTION RESUME API ERROR]', error);
            alert(error.message ?? '구독 재개에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
            setIsSubscriptionLoading(false);
        }
    };

    useEffect(() => {
        let isMounted = true;

        const fetchStreak = async () => {
            const cachedStreak = getCachedStreak(memberId);

            if (cachedStreak) {
                return;
            }

            try {
                const streakResponse = await getMyStreak({
                    memberId,
                    useCache: true,
                });

                if (!isMounted) {
                    return;
                }

                setStreak(normalizeStreak(streakResponse));
            } catch (error) {
                if (!isMounted) {
                    return;
                }

                console.error('[STREAK API ERROR]', error);
                setStreak(DEFAULT_STREAK);
            } finally {
                if (isMounted) {
                    setIsStreakLoading(false);
                }
            }
        };

        fetchStreak();

        return () => {
            isMounted = false;
        };
    }, [memberId]);

    useEffect(() => {
        let isMounted = true;

        const fetchHeatmap = async () => {
            const { startDate, endDate } = getMonthRange(selectedMonthCount);

            const cachedHeatmap = getCachedHeatmap({
                memberId,
                startDate,
                endDate,
            });

            if (cachedHeatmap) {
                return;
            }

            try {
                const heatmapResponse = await getMyHeatmap({
                    memberId,
                    startDate,
                    endDate,
                    useCache: true,
                });

                if (!isMounted) {
                    return;
                }

                setHeatmapItems(normalizeHeatmapItems(heatmapResponse));
            } catch (error) {
                if (!isMounted) {
                    return;
                }

                console.error('[HEATMAP API ERROR]', error);
                setHeatmapItems([]);
            } finally {
                if (isMounted) {
                    setIsHeatmapLoading(false);
                }
            }
        };

        fetchHeatmap();

        return () => {
            isMounted = false;
        };
    }, [memberId, selectedMonthCount]);

    useEffect(() => {
        let isMounted = true;

        const fetchRecentTils = async () => {
            if (!memberId) {
                return;
            }

            const cachedRecentTils = getCachedMyRecentTils(memberId);

            if (cachedRecentTils) {
                return;
            }

            try {
                const tilResponse = await getMemberTils(memberId, 0, 4);

                if (!isMounted) {
                    return;
                }

                setCachedMyRecentTils(memberId, tilResponse);
                setRecentTils(normalizeTilList(tilResponse));
            } catch (error) {
                if (!isMounted) {
                    return;
                }

                console.error('[MY RECENT TIL API ERROR]', error);
                setRecentTils([]);
            } finally {
                if (isMounted) {
                    setIsTilLoading(false);
                }
            }
        };

        fetchRecentTils();

        return () => {
            isMounted = false;
        };
    }, [memberId]);

    useEffect(() => {
        if (!isLoggedIn()) {
            navigate('/login', { replace: true });
            return;
        }

        let isMounted = true;

        const fetchSubscriptionAndPayback = async () => {
            try {
                const result = await loadSubscriptionAndPayback();

                if (!isMounted) {
                    return;
                }

                applySubscriptionAndPayback(result);
            } catch (error) {
                if (!isMounted) {
                    return;
                }

                console.error('[SUBSCRIPTION API ERROR]', error);
                setSubscription(null);
                setPayback(null);
            } finally {
                if (isMounted) {
                    setIsSubscriptionLoading(false);
                }
            }
        };

        fetchSubscriptionAndPayback();

        return () => {
            isMounted = false;
        };
    }, [navigate, loadSubscriptionAndPayback]);

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
                <div className="mypage-main-column">
                    <HeatmapSection
                        heatmapDays={heatmapDays}
                        selectedMonthCount={selectedMonthCount}
                        onChangeMonthCount={setSelectedMonthCount}
                        isLoading={isHeatmapLoading}
                    />

                    <WeeklyReportSection />
                </div>

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
                        memberId={memberId}
                    />
                </div>
            </section>
        </>
    );
};

export default MyPage;
