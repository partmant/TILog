import {
    useEffect,
    useMemo,
    useState,
} from 'react';
import {
    getCachedHeatmap,
    getMyHeatmap,
} from '../../api/myPageApi';
import {
    buildHeatmapDays,
    getMonthRange,
    normalizeHeatmapItems,
} from '../../utils/mypageUtils';

const getInitialHeatmapItems = (monthCount, memberId) => {
    const { startDate, endDate } = getMonthRange(monthCount);

    const cachedHeatmap = getCachedHeatmap({
        memberId,
        startDate,
        endDate,
    });

    return normalizeHeatmapItems(cachedHeatmap);
};

export const useMyPageHeatmap = (memberId, selectedMonthCount) => {
    const [heatmapItems, setHeatmapItems] = useState(() => {
        return getInitialHeatmapItems(selectedMonthCount, memberId);
    });

    const [isHeatmapLoading, setIsHeatmapLoading] = useState(() => {
        return getInitialHeatmapItems(selectedMonthCount, memberId).length === 0;
    });

    const heatmapDays = useMemo(
        () => buildHeatmapDays(heatmapItems, selectedMonthCount),
        [heatmapItems, selectedMonthCount]
    );

    const totalWriteCount = heatmapDays.reduce((sum, day) => sum + day.writeCount, 0);
    const writtenDays = heatmapDays.filter((day) => day.writeCount > 0).length;

    useEffect(() => {
        if (!memberId) {
            return;
        }

        const { startDate, endDate } = getMonthRange(selectedMonthCount);

        const cachedHeatmap = getCachedHeatmap({
            memberId,
            startDate,
            endDate,
        });

        if (cachedHeatmap) {
            return;
        }

        let isMounted = true;

        const fetchHeatmap = async () => {
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

    return {
        heatmapDays,
        totalWriteCount,
        writtenDays,
        isHeatmapLoading,
    };
};
