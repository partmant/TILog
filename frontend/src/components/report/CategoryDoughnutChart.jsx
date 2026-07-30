import { Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);

const COLORS = {
    BACKEND: '#8b5cf6',
    FRONTEND: '#06b6d4',
    SECURITY: '#f97316',
    CS: '#22c55e',
    OTHER: '#9ca3af',
};

const CategoryDoughnutChart = ({ categories }) => {
    const entries = Object.entries(categories)
        .filter(([, v]) => v > 0)
        .sort((a, b) => b[1] - a[1]);

    if (!entries.length) return null;

    const data = {
        labels: entries.map(([k]) => k),
        datasets: [{
            data: entries.map(([, v]) => v),
            backgroundColor: entries.map(([k]) => COLORS[k] || '#9ca3af'),
            borderWidth: 2,
            borderColor: '#fff',
        }],
    };

    const options = {
        cutout: '60%',
        plugins: {
            legend: { position: 'bottom', labels: { font: { size: 13 }, padding: 16 } },
            tooltip: {
                callbacks: {
                    label: (ctx) => ` ${ctx.label}: ${ctx.raw}%`,
                },
            },
        },
    };

    return <Doughnut data={data} options={options} />;
};

export default CategoryDoughnutChart;