import { Pie } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);

const COLORS = { EASY: '#22c55e', NORMAL: '#3b82f6', HARD: '#ef4444' };
const LABELS = { EASY: '쉬움', NORMAL: '보통', HARD: '어려움' };

const DifficultyPieChart = ({ distribution }) => {
    const entries = Object.entries(distribution).filter(([, v]) => v > 0);
    if (!entries.length) return null;

    const total = entries.reduce((s, [, v]) => s + v, 0);

    const data = {
        labels: entries.map(([k]) => LABELS[k] || k),
        datasets: [{
            data: entries.map(([, v]) => v),
            backgroundColor: entries.map(([k]) => COLORS[k] || '#9ca3af'),
            borderWidth: 2,
            borderColor: '#fff',
        }],
    };

    const options = {
        plugins: {
            legend: { position: 'bottom', labels: { font: { size: 13 }, padding: 16 } },
            tooltip: {
                callbacks: {
                    label: (ctx) => {
                        const count = ctx.raw;
                        const pct = Math.round(count / total * 100);
                        return ` ${ctx.label}: ${pct}% (${count}개)`;
                    },
                },
            },
        },
    };

    return <Pie data={data} options={options} />;
};

export default DifficultyPieChart;