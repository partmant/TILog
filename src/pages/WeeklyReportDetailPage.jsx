import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { getWeeklyReport, generateWeeklyReport } from '../api/weeklyReportApi';
import { toDateString, TEMP_MEMBER_ID } from '../utils/mypageUtils';
import '../styles/mypage/WeeklyReportDetail.css';

const DIFFICULTY_COLORS = { EASY: '#22c55e', NORMAL: '#3b82f6', HARD: '#ef4444' };
const DIFFICULTY_LABELS = { EASY: '쉬움', NORMAL: '보통', HARD: '어려움' };
const CATEGORY_COLORS = {
    BACKEND: '#8b5cf6',
    FRONTEND: '#06b6d4',
    SECURITY: '#f97316',
    CS: '#22c55e',
    OTHER: '#9ca3af',
};

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

        getWeeklyReport(TEMP_MEMBER_ID, weekStart)
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
            const data = await generateWeeklyReport(TEMP_MEMBER_ID, weekStart);
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
                            <span className="rd-error-msg">생성 중 오류가 발생했어요. 다시 시도해주세요.</span>
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

const ReportContent = ({ report }) => {
    const { weeklySummary, techStackDistribution, ruleBasedComment, aiAnalysisComment } = report;

    const categories = techStackDistribution?.categories || {};
    const tags = techStackDistribution?.tags || {};
    const difficultyDist = weeklySummary?.difficultyDistribution || {};
    const diffTotal = Object.values(difficultyDist).reduce((a, b) => a + b, 0);
    const topTags = Object.entries(tags).sort((a, b) => b[1] - a[1]).slice(0, 5);
    const topCategory = Object.entries(categories).sort((a, b) => b[1] - a[1])[0]?.[0] || '-';

    return (
        <div className="rd-content">

            {/* 핵심 지표 */}
            <div className="rd-stat-cards">
                <div className="rd-stat-card">
                    <span className="rd-stat-value">{weeklySummary?.totalPosts ?? 0}</span>
                    <span className="rd-stat-label">TIL 작성</span>
                </div>
                <div className="rd-stat-card">
                    <span className="rd-stat-value">{weeklySummary?.totalLearningTimeMinutes ?? 0}</span>
                    <span className="rd-stat-label">학습 시간(분)</span>
                </div>
                <div className="rd-stat-card">
                    <span className="rd-stat-value">{topCategory}</span>
                    <span className="rd-stat-label">주요 카테고리</span>
                </div>
            </div>

            {/* 난이도 분포 */}
            {diffTotal > 0 && (
                <div className="rd-panel">
                    <h3>난이도 분포</h3>
                    <div className="rd-bar-stacked">
                        {Object.entries(difficultyDist).map(([level, count]) => (
                            <div
                                key={level}
                                style={{
                                    width: `${(count / diffTotal) * 100}%`,
                                    background: DIFFICULTY_COLORS[level] || '#9ca3af',
                                }}
                                title={`${DIFFICULTY_LABELS[level] || level}: ${count}개`}
                            />
                        ))}
                    </div>
                    <div className="rd-bar-legend">
                        {Object.entries(difficultyDist).map(([level, count]) => (
                            <span key={level}>
                                <i style={{ background: DIFFICULTY_COLORS[level] || '#9ca3af' }} />
                                {DIFFICULTY_LABELS[level] || level} {count}개 ({Math.round(count / diffTotal * 100)}%)
                            </span>
                        ))}
                    </div>
                </div>
            )}

            {/* 기술 스택 분포 */}
            {Object.keys(categories).length > 0 && (
                <div className="rd-panel">
                    <h3>기술 스택 분포</h3>
                    <div className="rd-category-bars">
                        {Object.entries(categories)
                            .sort((a, b) => b[1] - a[1])
                            .map(([cat, pct]) => (
                                <div key={cat} className="rd-category-row">
                                    <span className="rd-category-label">{cat}</span>
                                    <div className="rd-category-track">
                                        <div
                                            className="rd-category-fill"
                                            style={{
                                                width: `${pct}%`,
                                                background: CATEGORY_COLORS[cat] || '#9ca3af',
                                            }}
                                        />
                                    </div>
                                    <span className="rd-category-pct">{pct}%</span>
                                </div>
                            ))}
                    </div>

                    {topTags.length > 0 && (
                        <div className="rd-top-tags">
                            <span className="rd-top-tags-label">상위 태그</span>
                            {topTags.map(([tag, count]) => (
                                <span key={tag} className="rd-tag-chip">{tag} {count}</span>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* 규칙 기반 코멘트 */}
            {ruleBasedComment && (
                <div className="rd-panel">
                    <h3>💬 AI 코멘트</h3>
                    <blockquote className="rd-rule-comment">{ruleBasedComment}</blockquote>
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
                        <span>LLM 연동 완료 후 자동으로 채워집니다.</span>
                    </div>
                )}
            </div>

        </div>
    );
};

export default WeeklyReportDetailPage;