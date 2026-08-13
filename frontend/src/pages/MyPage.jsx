import { useState } from 'react';
import MyPageHero from '../components/mypage/MyPageHero';
import MyPageStats from '../components/mypage/MyPageStats';
import HeatmapSection from '../components/mypage/HeatmapSection';
import RecentTilSection from '../components/mypage/RecentTilSection';
import BookmarkedTilSection from '../components/mypage/BookmarkedTilSection';
import DraftTilSection from '../components/mypage/DraftTilSection';
import SubscriptionPaybackSection from '../components/mypage/SubscriptionPaybackSection';
import WeeklyReportSection from '../components/mypage/WeeklyReportSection';
import { useMyPageStreak } from '../hooks/mypage/useMyPageStreak';
import { useMyPageHeatmap } from '../hooks/mypage/useMyPageHeatmap';
import { useMyRecentTils } from '../hooks/mypage/useMyRecentTils';
import { useSubscriptionPayback } from '../hooks/mypage/useSubscriptionPayback';
import { getMemberId } from '../utils/authUtils';

const MyPage = () => {
    const memberId = getMemberId();
    const [selectedMonthCount, setSelectedMonthCount] = useState(6);

    const {
        streak,
        isStreakLoading,
    } = useMyPageStreak(memberId);

    const {
        heatmapDays,
        totalWriteCount,
        writtenDays,
        isHeatmapLoading,
    } = useMyPageHeatmap(memberId, selectedMonthCount);

    const {
        recentTils,
        isTilLoading,
    } = useMyRecentTils(memberId);

    const {
        subscription,
        payback,
        isSubscriptionLoading,
        isSubscriptionActionLoading,
        handleSubscribe,
        handleCancelSubscription,
        handleResumeSubscription,
    } = useSubscriptionPayback();

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

                    <DraftTilSection />

                    <BookmarkedTilSection />
                </div>
            </section>
        </>
    );
};

export default MyPage;
