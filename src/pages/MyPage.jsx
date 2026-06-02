import {
    useEffect,
    useMemo,
    useState,
} from 'react';
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
import MyPageHeader from '../components/mypage/MyPageHeader';
import MyPageHero from '../components/mypage/MyPageHero';
import MyPageStats from '../components/mypage/MyPageStats';
import HeatmapSection from '../components/mypage/HeatmapSection';
import RecentTilSection from '../components/mypage/RecentTilSection';
import CheerCard from '../components/mypage/CheerCard';
import {
    TEMP_MEMBER_ID,
    buildHeatmapDays,
    getMonthRange,
    normalizeHeatmapItems,
    normalizeTilList,
} from '../utils/mypageUtils';
import '../styles/mypage/MyPage.css';

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

const getInitialHeatmapItems = (monthCount) => {
    const { startDate, endDate } = getMonthRange(monthCount);

    const cachedHeatmap = getCachedHeatmap({
        memberId: TEMP_MEMBER_ID,
        startDate,
        endDate,
    });

    return normalizeHeatmapItems(cachedHeatmap);
};

const getInitialStreak = () => {
    return normalizeStreak(getCachedStreak(TEMP_MEMBER_ID));
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
    const [selectedMonthCount, setSelectedMonthCount] = useState(6);

    const [streak, setStreak] = useState(() => getInitialStreak());
    const [heatmapItems, setHeatmapItems] = useState(() => getInitialHeatmapItems(6));
    const [recentTils, setRecentTils] = useState(() => getInitialRecentTils());

    const [isStreakLoading, setIsStreakLoading] = useState(() => {
        return getCachedStreak(TEMP_MEMBER_ID) === null;
    });

    const [isHeatmapLoading, setIsHeatmapLoading] = useState(() => {
        return getInitialHeatmapItems(6).length === 0;
    });

    const [isTilLoading, setIsTilLoading] = useState(() => {
        return getInitialRecentTils().length === 0;
    });

    const heatmapDays = useMemo(
        () => buildHeatmapDays(heatmapItems, selectedMonthCount),
        [heatmapItems, selectedMonthCount]
    );

    const totalWriteCount = heatmapDays.reduce((sum, day) => sum + day.writeCount, 0);
    const writtenDays = heatmapDays.filter((day) => day.writeCount > 0).length;

    useEffect(() => {
        const fetchStreak = async () => {
            const cachedStreak = getCachedStreak(TEMP_MEMBER_ID);

            if (cachedStreak) {
                setStreak(normalizeStreak(cachedStreak));
                setIsStreakLoading(false);
                return;
            }

            try {
                setIsStreakLoading(true);

                const streakResponse = await getMyStreak({
                    memberId: TEMP_MEMBER_ID,
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
                memberId: TEMP_MEMBER_ID,
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
                    memberId: TEMP_MEMBER_ID,
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

    return (
        <main className="mypage">
            <div className="mypage-shell">
                <MyPageHeader />

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

                    <RecentTilSection
                        recentTils={recentTils}
                        isLoading={isTilLoading}
                    />
                </section>

                <CheerCard />
            </div>
        </main>
    );
};

export default MyPage;
