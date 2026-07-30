import { useNavigate } from 'react-router-dom';
import { useWeeklyReport } from '../../hooks/report/useWeeklyReport.js';
import '../../styles/mypage/WeeklyReportSection.css';
import {isPremiumUser} from "../../utils/authUtils.js";

const IS_PREMIUM = isPremiumUser;

const DIFFICULTY_COLORS = { EASY: '#22c55e', NORMAL: '#3b82f6', HARD: '#ef4444' };
const DIFFICULTY_LABELS = { EASY: '쉬움', NORMAL: '보통', HARD: '어려움' };

const formatWeekRange = (weekStartDate) => {
    const start = new Date(weekStartDate + 'T00:00:00');
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    const fmt = (d) => `${d.getMonth() + 1}/${d.getDate()}`;
    return `${start.getFullYear()}년 ${fmt(start)} ~ ${fmt(end)}`;
};

const getTopCategory = (categories) => {
    if (!categories) return '-';
    const entries = Object.entries(categories);
    if (!entries.length) return '-';
    return entries.sort((a, b) => b[1] - a[1])[0][0];
};

const DifficultyBar = ({ distribution }) => {
    const total = Object.values(distribution).reduce((a, b) => a + b, 0);
    if (total === 0) return null;
    return (
        <div className="wr-difficulty-wrapper">
            <div className="wr-difficulty-bar">
                {Object.entries(distribution).map(([level, count]) => (
                    <div
                        key={level}
                        className="wr-difficulty-segment"
                        style={{ width: `${(count / total) * 100}%`, background: DIFFICULTY_COLORS[level] || '#9ca3af' }}
                        title={`${DIFFICULTY_LABELS[level] || level}: ${count}개`}
                    />
                ))}
            </div>
            <div className="wr-difficulty-legend">
                {Object.entries(distribution).map(([level, count]) => (
                    <span key={level}>
                        <i style={{ background: DIFFICULTY_COLORS[level] || '#9ca3af' }} />
                        {DIFFICULTY_LABELS[level] || level} {count}개
                    </span>
                ))}
            </div>
        </div>
    );
};

const WeeklyReportSection = () => {
    const navigate = useNavigate();
    const { report, status, lastMonday, generateReport } = useWeeklyReport();

    if (!IS_PREMIUM) {
        return (
            <section className="mypage-panel wr-section wr-locked">
                <span className="wr-lock-icon">🔒</span>
                <p>AI 성장 리포트는 프리미엄 전용 기능입니다.</p>
            </section>
        );
    }

    return (
        <section className="mypage-panel wr-section">
            <div className="mypage-panel-header">
                <div>
                    <h2>AI 주간 성장 리포트</h2>
                    <p>{lastMonday ? formatWeekRange(lastMonday) : '지난 주'} 기록 분석</p>
                </div>
                {status === 'done' && report && (
                    <button
                        className="wr-detail-btn"
                        onClick={() => navigate(`/weekly-report?weekStart=${lastMonday}`)}
                    >
                        상세 보기 →
                    </button>
                )}
            </div>

            {status === 'loading' && (
                <div className="wr-state-box">리포트를 확인하는 중입니다...</div>
            )}

            {status === 'idle' && (
                <div className="wr-state-box">
                    <p className="wr-state-desc">지난주의 TIL 기록을 AI가 분석해드려요.</p>
                    <button className="wr-generate-btn" onClick={generateReport}>
                        지난주 리포트 생성하기
                    </button>
                </div>
            )}

            {status === 'generating' && (
                <div className="wr-state-box wr-generating">
                    <div className="wr-spinner" />
                    <p>AI가 지난주 기록을 분석하고 있어요...</p>
                    <span>잠시만 기다려주세요.</span>
                </div>
            )}

            {status === 'noPost' && (
                <div className="wr-state-box">
                    <p className="wr-state-desc">해당 주에 작성한 TIL이 없어서<br />주간 성장 리포트를 생성할 수 없어요 😢</p>
                </div>
            )}

            {status === 'error' && (
                <div className="wr-state-box">
                    <p className="wr-state-desc">지금은 리포트를 생성할 수 없어요. 나중에 다시 시도해주세요.</p>
                    <button className="wr-generate-btn" onClick={generateReport}>
                        다시 시도하기
                    </button>
                </div>
            )}

            {status === 'done' && report && (
                <div className="wr-summary">

                    {/* 페르소나 타이틀 */}
                    {report.parsedAiAnalysis?.weekly_persona?.title && (
                        <div className="wr-persona-banner">
                            이번 주 나는: <strong>{report.parsedAiAnalysis.weekly_persona.title}</strong>
                        </div>
                    )}

                    {/* stat 3개 */}
                    <div className="wr-stats">
                        <div className="wr-stat">
                            <span className="wr-stat-value">{report.weeklySummary?.totalPosts ?? 0}</span>
                            <span className="wr-stat-label">TIL 작성</span>
                        </div>
                        <div className="wr-stat">
                            <span className="wr-stat-value">{report.weeklySummary?.totalLearningTimeMinutes ?? 0}</span>
                            <span className="wr-stat-label">학습 시간(분)</span>
                        </div>
                        <div className="wr-stat">
                            <span className="wr-stat-value">{getTopCategory(report.techStackDistribution?.categories)}</span>
                            <span className="wr-stat-label">주요 카테고리</span>
                        </div>
                    </div>

                    {/* 난이도 바 */}
                    {report.weeklySummary?.difficultyDistribution && (
                        <DifficultyBar distribution={report.weeklySummary.difficultyDistribution} />
                    )}

                    {/* 규칙 기반 코멘트 */}
                    {report.ruleBasedComment && (
                        <blockquote className="wr-comment">{report.ruleBasedComment}</blockquote>
                    )}

                    {/* AI 격려 메시지 티저 */}
                    {report.parsedAiAnalysis?.mentor_cheering_message && (
                        <div className="wr-cheer">
                            <span>💌</span>
                            <p>{report.parsedAiAnalysis.mentor_cheering_message}</p>
                        </div>
                    )}

                </div>
            )}
        </section>
    );
};

export default WeeklyReportSection;