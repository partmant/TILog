import {
    useEffect,
    useState,
} from 'react';
import { getMemberTils } from '../../api/feed';
import { normalizeTilList } from '../../utils/mypageUtils';
import {
    getCachedMyRecentTils,
    getInitialRecentTils,
    setCachedMyRecentTils,
} from '../../utils/mypageRecentTilCache';

export const useMyRecentTils = (memberId) => {
    const [recentTils, setRecentTils] = useState(() => {
        return getInitialRecentTils(memberId);
    });

    const [isTilLoading, setIsTilLoading] = useState(() => {
        return getInitialRecentTils(memberId).length === 0;
    });

    useEffect(() => {
        if (!memberId) {
            return;
        }

        const cachedRecentTils = getCachedMyRecentTils(memberId);

        if (cachedRecentTils) {
            return;
        }

        let isMounted = true;

        const fetchRecentTils = async () => {
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

    return {
        recentTils,
        isTilLoading,
    };
};
