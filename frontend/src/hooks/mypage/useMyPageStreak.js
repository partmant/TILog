import {
    useEffect,
    useState,
} from 'react';
import {
    getCachedStreak,
    getMyStreak,
} from '../../api/myPageApi';

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

const getInitialStreak = (memberId) => {
    return normalizeStreak(getCachedStreak(memberId));
};

export const useMyPageStreak = (memberId) => {
    const [streak, setStreak] = useState(() => getInitialStreak(memberId));

    const [isStreakLoading, setIsStreakLoading] = useState(() => {
        return getCachedStreak(memberId) === null;
    });

    useEffect(() => {
        if (!memberId) {
            return;
        }

        const cachedStreak = getCachedStreak(memberId);

        if (cachedStreak) {
            return;
        }

        let isMounted = true;

        const fetchStreak = async () => {
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

    return {
        streak,
        isStreakLoading,
    };
};
