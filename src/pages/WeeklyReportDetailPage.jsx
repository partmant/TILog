import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getWeeklyReport, generateWeeklyReport } from '../api/weeklyReportApi';
import { toDateString } from '../utils/mypageUtils';
import DifficultyPieChart from '../components/report/DifficultyPieChart';
import CategoryDoughnutChart from '../components/report/CategoryDoughnutChart';
import TechStackBarChart from '../components/report/TechStackBarChart';
import '../styles/mypage/WeeklyReportDetail.css';

const getLastMonday = () => {
    const today = new Date();
    const day = today.getDay();
    const d = new Date(today);
    d.setDate(today.getDate() - (day === 0 ? 6 : day - 1) - 7);
    return toDateString(d);
};

const addDays = (dateStr, days) => {
    const d = new Date(dateStr + 'T00:00:00');
    d.setDate(d.getDate() + days);
    return toDateString(d);
};

const formatWeekRange = (weekStartDate) => {
    const start = new Date(weekStartDate + 'T00:00:00');
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    const fmt = (d) => `${d.getMonth() + 1}월 ${d.getDate()}일`;
    return `${start.getFullYear()}년 ${fmt(start)} ~ ${fmt(end)}`;
};

// status: 'loading' | 'done' | 'noReport' | 'generating' | 'error'
const WeeklyReportDetailPage = () => {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();

    const lastMonday = getLastMonday();
    const weekStart = searchParams.get('weekStart') || lastMonday;

    const [report, setReport] = useState(null);
    const [status, setStatus] = useState('loading');

    const prevWeek = addDays(weekStart, -7);
    const nextWeek = addDays(weekStart, 7);
    const canGoNext = nextWeek <= lastMonday;
    const canGenerate = weekStart <= lastMonday;

    useEffect(() => {
        let cancelled = false;
        setStatus('loading');
        setReport(null);

        getWeeklyReport(weekStart)
            .then((data) => {
                if (cancelled) return;
                setReport(data);
                setStatus(data ? 'done' : 'noReport');
            })
            .catch(() => {
                if (!cancelled) setStatus('noReport');
            });

        return () => { cancelled = true; };
    }, [weekStart]);

    const handleGenerate = async () => {
        setStatus('generating');
        try {
            const data = await generateWeeklyReport(weekStart);
            setReport(data);
            setStatus('done');
        } catch {
            setStatus('error');
        }
    };

    return (
        <div className="rd-page">
            <div className="rd-shell">

                <div className="rd-header">
                    <button className="rd-back-btn" onClick={() => navigate('/mypage')}>
                        ← 마이페이지
                    </button>
                    <div className="rd-week-nav">
                        <button className="rd-nav-btn" onClick={() => setSearchParams({ weekStart: prevWeek })}>
                            ← 이전 주
                        </button>
                        <span className="rd-week-label">{formatWeekRange(weekStart)}</span>
                        <button
                            className="rd-nav-btn"
                            onClick={() => setSearchParams({ weekStart: nextWeek })}
                            disabled={!canGoNext}
                        >
                            다음 주 →
                        </button>
                    </div>
                </div>

                {status === 'loading' && (
                    <div className="rd-center-state">
                        <div className="rd-spinner" />
                        <p>리포트를 불러오는 중입니다...</p>
                    </div>
                )}

                {(status === 'noReport' || status === 'error') && (
                    <div className="rd-center-state">
                        <p className="rd-empty-title">이 주의 리포트가 없어요.</p>
                        {canGenerate && (
                            <button className="rd-generate-btn" onClick={handleGenerate}>
                                리포트 생성하기
                            </button>
                        )}
                        {status === 'error' && (
                            <span className="rd-error-msg">지금은 생성할 수 없어요. 나중에 다시 시도해주세요.</span>
                        )}
                    </div>
                )}

                {status === 'generating' && (
                    <div className="rd-center-state">
                        <div className="rd-spinner" />
                        <p>AI가 기록을 분석하고 있어요...</p>
                        <span>잠시만 기다려주세요.</span>
                    </div>
                )}

                {status === 'done' && report && <ReportContent report={report} />}

            </div>
        </div>
    );
};

const RANK_MEDAL = ['🥇', '🥈', '🥉'];

const ReportContent = ({ report }) => {
    const { weeklySummary, techStackDistribution, cumulativeData, ruleBasedComment, aiAnalysisComment } = report;

    const categories     = techStackDistribution?.categories || {};
    const tags           = techStackDistribution?.tags || {};
    const difficultyDist = weeklySummary?.difficultyDistribution || {};
    const cumulativeTags = cumulativeData?.tagTotals || {};

    // 1행 stat 카드용
    const thisWeekPosts   = weeklySummary?.totalPosts ?? 0;
    const thisWeekMinutes = weeklySummary?.totalLearningTimeMinutes ?? 0;
    const cumPosts        = cumulativeData?.totalPosts ?? 0;
    const cumMinutes      = cumulativeData?.totalLearningMinutes ?? 0;
    const cumTopCat       = Object.entries(cumulativeData?.categoryDistribution || {}).sort((a, b) => b[1] - a[1])[0]?.[0] || '-';
    const thisWeekTopCat  = Object.entries(categories).sort((a, b) => b[1] - a[1])[0]?.[0] || '-';
    const catSub          = thisWeekTopCat === cumTopCat ? '이번 주도 동일' : `이번 주 ${thisWeekTopCat}`;

    // 2행 Top3 카드용
    const top3Tags = Object.entries(cumulativeTags).sort((a, b) => b[1] - a[1]).slice(0, 3);

    const hasDifficulty = Object.values(difficultyDist).some((v) => v > 0);
    const hasCategories = Object.keys(categories).length > 0;
    const hasTags       = Object.keys(tags).length > 0 || Object.keys(cumulativeTags).length > 0;

    return (
        <div className="rd-content">

            {/* 1행: 누적 stat 카드 (큰 숫자) + 이번 주 서브 */}
            <div className="rd-stat-cards">
                <div className="rd-stat-card">
                    <span className="rd-stat-label-top">이번 주 TIL</span>
                    <span className="rd-stat-value">{thisWeekPosts}</span>
                    <span className="rd-stat-sub">누적 {cumPosts}개</span>
                </div>
                <div className="rd-stat-card">
                    <span className="rd-stat-label-top">이번 주 학습</span>
                    <span className="rd-stat-value">
                        {thisWeekMinutes}<small className="rd-stat-unit">분</small>
                    </span>
                    <span className="rd-stat-sub">누적 {cumMinutes}분</span>
                </div>
                <div className="rd-stat-card">
                    <span className="rd-stat-label-top">주요 카테고리</span>
                    <span className="rd-stat-value rd-stat-category">{cumTopCat}</span>
                    <span className="rd-stat-sub">{catSub}</span>
                </div>
            </div>

            {/* 2행: 파이차트 | 도넛차트 | Top3 태그 */}
            <div className="rd-charts-row">
                {hasDifficulty && (
                    <div className="rd-panel rd-chart-third">
                        <h3>난이도 분포</h3>
                        <div className="rd-chart-container">
                            <DifficultyPieChart distribution={difficultyDist} />
                        </div>
                    </div>
                )}
                {hasCategories && (
                    <div className="rd-panel rd-chart-third">
                        <h3>카테고리 분포</h3>
                        <div className="rd-chart-container">
                            <CategoryDoughnutChart categories={categories} />
                        </div>
                    </div>
                )}
                <div className="rd-panel rd-chart-third">
                    <h3>많이 쓴 기술 TOP 3</h3>
                    {top3Tags.length > 0 ? (
                        <div className="rd-top3-list">
                            {top3Tags.map(([tag, count], i) => (
                                <div key={tag} className="rd-top3-item">
                                    <span className="rd-top3-medal">{RANK_MEDAL[i]}</span>
                                    <span className="rd-top3-name">{tag}</span>
                                    <span className="rd-top3-count">누적 {count}회</span>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="rd-top3-empty">아직 기록이 없어요.</p>
                    )}
                </div>
            </div>

            {/* 3행 규칙 기반 코멘트 */}
            {ruleBasedComment && (
                <div className="rd-panel">
                    <h3>💬 활동 요약 </h3>
                    <blockquote className="rd-rule-comment">{ruleBasedComment}</blockquote>
                </div>
            )}

            {/* 4행: 기술 스택 막대 + 누적 기준선 */}
            {hasTags && (
                <div className="rd-panel">
                    <h3>기술 스택 분포</h3>
                    <p className="rd-chart-hint">막대: 이번 주 비율 / 점선: 누적 비율 · 호버 시 이번 주 횟수 확인</p>
                    <div className="rd-techstack-chart">
                        <TechStackBarChart thisWeekTags={tags} cumulativeTags={cumulativeTags} />
                    </div>
                </div>
            )}



            {/* AI 심층 분석 */}
            <div className="rd-panel rd-ai-panel">
                <h3>🤖 AI 심층 분석</h3>
                {aiAnalysisComment ? (
                    <p className="rd-ai-comment">{aiAnalysisComment}</p>
                ) : (
                    <div className="rd-ai-placeholder">
                        <p>AI 심층 분석이 곧 제공될 예정이에요. ✨</p>
                    </div>
                )}
            </div>

        </div>
    );
};

export default WeeklyReportDetailPage;