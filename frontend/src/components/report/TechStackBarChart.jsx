import { Bar } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    CategoryScale, LinearScale, BarElement,
    LineElement, PointElement,
    Tooltip, Legend,
} from 'chart.js';

ChartJS.register(
    CategoryScale, LinearScale, BarElement,
    LineElement, PointElement,
    Tooltip, Legend,
);

const MAX_ITEMS = 12;
const MIN_ITEMS = 5;

/**
 * x축 태그 목록을 구성한다.
 *  - 이번 주 사용 태그 우선(이번 주 횟수 내림차순)
 *  - 누적에만 있는 태그를 뒤에 붙임(누적 횟수 내림차순)
 *  - MAX_ITEMS 초과 시: 이번 주 태그 전부 유지 + 누적만 있는 태그는 누적 적은 순서로 생략
 *  - MIN_ITEMS 미만 시: 누적 태그로 채우고 이번 주 막대는 0
 */
function buildTagLabels(thisWeekTags, cumulativeTags) {
    const thisWeekKeys = Object.keys(thisWeekTags).sort(
        (a, b) => thisWeekTags[b] - thisWeekTags[a],
    );

    const cumulativeOnlyKeys = Object.keys(cumulativeTags)
        .filter((k) => !thisWeekTags[k])
        .sort((a, b) => cumulativeTags[b] - cumulativeTags[a]);

    let labels = [...thisWeekKeys, ...cumulativeOnlyKeys];

    if (labels.length > MAX_ITEMS) {
        // 이번 주 태그는 전부 유지, 초과분은 누적 적은 태그부터 제거
        const overflow = labels.length - MAX_ITEMS;
        const cumulativeOnlyTrimmed = cumulativeOnlyKeys.slice(0, cumulativeOnlyKeys.length - overflow);
        labels = [...thisWeekKeys, ...cumulativeOnlyTrimmed];
    }

    if (labels.length < MIN_ITEMS) {
        const extra = cumulativeOnlyKeys
            .filter((k) => !labels.includes(k))
            .slice(0, MIN_ITEMS - labels.length);
        labels = [...labels, ...extra];
    }

    return labels;
}

/** 횟수 맵을 비율(%) 맵으로 변환 */
function toPercent(countMap) {
    const total = Object.values(countMap).reduce((s, v) => s + v, 0);
    if (total === 0) return {};
    return Object.fromEntries(
        Object.entries(countMap).map(([k, v]) => [k, Math.round(v / total * 100)]),
    );
}

const TechStackBarChart = ({ thisWeekTags, cumulativeTags }) => {
    if (!thisWeekTags || !cumulativeTags) return null;

    const labels = buildTagLabels(thisWeekTags, cumulativeTags);
    if (!labels.length) return null;

    const thisWeekPct = toPercent(thisWeekTags);
    const cumulativePct = toPercent(cumulativeTags);

    const thisWeekData = labels.map((tag) => thisWeekPct[tag] ?? 0);
    const cumulativeData = labels.map((tag) => cumulativePct[tag] ?? 0);

    const data = {
        labels,
        datasets: [
            {
                type: 'bar',
                label: '이번 주',
                data: thisWeekData,
                backgroundColor: '#6366f1cc',
                borderColor: '#6366f1',
                borderWidth: 1,
                borderRadius: 4,
                order: 2,
            },
            {
                type: 'line',
                label: '누적',
                data: cumulativeData,
                borderColor: '#9ca3af',
                borderWidth: 2,
                borderDash: [5, 4],
                pointBackgroundColor: '#9ca3af',
                pointRadius: 4,
                fill: false,
                order: 1,
            },
        ],
    };

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
            legend: {
                position: 'top',
                labels: { font: { size: 13 }, padding: 16 },
            },
            tooltip: {
                callbacks: {
                    // 이번 주 막대: 퍼센트와 실제 횟수 같이 표시
                    label: (ctx) => {
                        if (ctx.dataset.type === 'bar') {
                            const tag = labels[ctx.dataIndex];
                            const count = thisWeekTags[tag] ?? 0;
                            return ` 이번 주: ${ctx.raw}% (${count}회)`;
                        }
                        return ` 누적: ${ctx.raw}%`;
                    },
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: { callback: (v) => `${v}%` },
                grid: { color: '#f3f4f6' },
            },
            x: {
                grid: { display: false },
                ticks: { font: { size: 12 } },
            },
        },
    };

    return <Bar data={data} options={options} />;
};

export default TechStackBarChart;