export const TEMP_MEMBER_ID = 1;

export const toDateString = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
};

export const getMonthRange = (monthCount = 6) => {
    const now = new Date();
    const endDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startDate = new Date(now.getFullYear(), now.getMonth() - monthCount + 1, 1);

    return {
        startDate: toDateString(startDate),
        endDate: toDateString(endDate),
    };
};

export const normalizeHeatmapItems = (heatmapResponse) => {
    if (!heatmapResponse) return [];
    if (Array.isArray(heatmapResponse)) return heatmapResponse;

    return (
        heatmapResponse.heatmap ||
        heatmapResponse.heatmapData ||
        heatmapResponse.dailyCounts ||
        heatmapResponse.items ||
        heatmapResponse.days ||
        []
    );
};

export const normalizeTilList = (pageResponse) => {
    if (!pageResponse) return [];
    if (Array.isArray(pageResponse)) return pageResponse;

    return pageResponse.content || pageResponse.items || pageResponse.tils || [];
};

export const getCountFromHeatmapItem = (item) => {
    return item.writeCount ?? item.count ?? item.tilCount ?? item.value ?? 0;
};

export const getDateFromHeatmapItem = (item) => {
    return item.date ?? item.writtenDate ?? item.localDate;
};

export const buildHeatmapDays = (items, monthCount = 6) => {
    const { startDate, endDate } = getMonthRange(monthCount);

    const countMap = new Map(
        items
            .filter((item) => getDateFromHeatmapItem(item))
            .map((item) => [getDateFromHeatmapItem(item), getCountFromHeatmapItem(item)])
    );

    const days = [];
    const start = new Date(startDate);
    const end = new Date(endDate);

    for (let date = new Date(start); date <= end; date.setDate(date.getDate() + 1)) {
        const dateText = toDateString(date);

        days.push({
            date: dateText,
            writeCount: countMap.get(dateText) ?? 0,
            year: date.getFullYear(),
            month: date.getMonth() + 1,
            day: date.getDate(),
        });
    }

    return days;
};

export const buildMonthlyHeatmap = (days) => {
    if (!days || days.length === 0) {
        return [];
    }

    const monthMap = new Map();

    days.forEach((day) => {
        const key = `${day.year}-${String(day.month).padStart(2, '0')}`;

        if (!monthMap.has(key)) {
            monthMap.set(key, []);
        }

        monthMap.get(key).push(day);
    });

    return Array.from(monthMap.entries()).map(([key, monthDays]) => {
        const firstDay = monthDays[0];
        const year = firstDay.year;
        const month = firstDay.month;

        const firstDate = new Date(year, month - 1, 1);
        const lastDate = new Date(year, month, 0);
        const firstWeekday = firstDate.getDay();
        const lastDay = lastDate.getDate();

        const dayMap = new Map(monthDays.map((day) => [day.day, day]));
        const cells = [];

        for (let i = 0; i < firstWeekday; i += 1) {
            cells.push(null);
        }

        for (let dayNumber = 1; dayNumber <= lastDay; dayNumber += 1) {
            const existing = dayMap.get(dayNumber);

            if (existing) {
                cells.push(existing);
            } else {
                cells.push({
                    date: `${year}-${String(month).padStart(2, '0')}-${String(dayNumber).padStart(2, '0')}`,
                    writeCount: 0,
                    year,
                    month,
                    day: dayNumber,
                });
            }
        }

        while (cells.length % 7 !== 0) {
            cells.push(null);
        }

        const weeks = [];

        for (let i = 0; i < cells.length; i += 7) {
            weeks.push(cells.slice(i, i + 7));
        }

        return {
            key,
            year,
            month,
            label: `${month}월`,
            weeks,
        };
    });
};

export const getHeatLevel = (count) => {
    if (count >= 4) return 4;
    if (count >= 3) return 3;
    if (count >= 2) return 2;
    if (count >= 1) return 1;

    return 0;
};

export const getTilTitle = (til) => {
    return til.title ?? til.postTitle ?? til.name ?? '제목 없음';
};

export const getTilCategory = (til) => {
    return til.difficulty ?? til.category ?? til.tagName ?? 'TIL';
};

export const getTilDate = (til) => {
    const rawDate = til.createdAt ?? til.createdDate ?? til.writtenDate ?? til.created_at;

    if (!rawDate) return '-';

    return rawDate.toString().slice(0, 10).replaceAll('-', '.');
};

export const getTilMeta = (til) => {
    const likeCount = til.likeCount ?? til.likes ?? 0;
    const commentCount = til.commentCount ?? til.comments ?? 0;
    const nickname = til.nickname ?? til.authorNickname ?? '작성자';

    return `${nickname} · 댓글 ${commentCount} · 좋아요 ${likeCount}`;
};
