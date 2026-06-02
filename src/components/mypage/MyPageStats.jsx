import StatCard from './StatCard';
import '../../styles/mypage/MyPageStats.css';

const MyPageStats = ({ streak, totalWriteCount, writtenDays, isLoading }) => {
    if (isLoading) {
        return (
            <section className="mypage-stats">
                <div className="mypage-stat-skeleton" />
                <div className="mypage-stat-skeleton" />
                <div className="mypage-stat-skeleton" />
                <div className="mypage-stat-skeleton" />
            </section>
        );
    }

    return (
        <section className="mypage-stats">
            <StatCard
                icon="✎"
                label="총 작성 TIL 수"
                value={streak.totalTilCount || totalWriteCount}
                unit="개"
                description="누적 TIL 작성 개수"
                tone="purple"
            />

            <StatCard
                icon="▦"
                label="작성한 날짜 수"
                value={streak.totalWrittenDays || writtenDays}
                unit="일"
                description="TIL을 작성한 날짜"
                tone="blue"
            />

            <StatCard
                icon="●"
                label="현재 연속 작성"
                value={streak.currentStreak}
                unit="일"
                description="현재 이어지고 있는 스트릭"
                tone="orange"
            />

            <StatCard
                icon="★"
                label="최장 연속 작성"
                value={streak.longestStreak}
                unit="일"
                description="가장 길게 이어진 스트릭"
                tone="green"
            />
        </section>
    );
};

export default MyPageStats;
