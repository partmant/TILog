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

// status: 'loading' | 'done' | 'noReport' | 'generating' | 'error' | 'noPost'
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
        } catch (err) {
            try {
                const body = JSON.parse(err.message);
                if (body?.message?.includes('TIL이 없어')) {
                    setStatus('noPost');
                    return;
                }
            } catch {}
            setStatus('error');
        }
    };

    return (
        <div className="rd-page">
            <div className="rd-shell">

                <div className="rd-page-title">
                    <h1 className="rd-page-title-text">주간 성장 리포트</h1>
                    <p className="rd-page-title-sub">AI가 분석한 나의 학습 기록</p>
                </div>

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
                    {status === 'done' && report && (
                        <button className="rd-export-btn" onClick={() => window.print()}>
                            PDF 저장
                        </button>
                    )}
                </div>

                {status === 'loading' && (
                    <div className="rd-center-state">
                        <div className="rd-spinner" />
                        <p>리포트를 불러오는 중입니다...</p>
                    </div>
                )}

                {status === 'noPost' && (
                    <div className="rd-center-state">
                        <p className="rd-empty-title">해당 주에 작성한 TIL이 없어서<br />주간 성장 리포트를 생성할 수 없어요 😢</p>
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

const CAREER_STATUS_META = {
    BALANCED:      { label: '균형', className: 'rd-badge-balanced' },
    BIAS_WARNING:  { label: '편중 주의', className: 'rd-badge-warning' },
    INITIAL_STAGE: { label: '시작 단계', className: 'rd-badge-initial' },
};

const PORTFOLIO_ITEM_LABELS = {
    JOB_SEEKER:    { resumeKeyword: '추천 이력서 키워드',    interviewQuestion: '기술 면접 질문' },
    CAREER_CHANGE: { resumeKeyword: '추천 이력서 키워드',    interviewQuestion: '기술 면접 질문' },
    EMPLOYED:      { resumeKeyword: '아키텍처 개선',    interviewQuestion: '코드 리뷰 주제' },
    STUDENT:       { resumeKeyword: '추천 프로젝트 키워드',  interviewQuestion: '전공 지식 퀴즈' },
    FREELANCER:    { resumeKeyword: '기술 셀링 포인트', interviewQuestion: '요구사항 검증' },
};
const DEFAULT_PORTFOLIO_LABELS = { resumeKeyword: '기술 셀링 포인트', interviewQuestion: '요구사항 검증' };

const ReportContent = ({ report }) => {
    const {
        weeklySummary, techStackDistribution, cumulativeData,
        ruleBasedComment, aiAnalysisComment, parsedAiAnalysis, currentStatus,
    } = report;

    const categories     = techStackDistribution?.categories || {};
    const tags           = techStackDistribution?.tags || {};
    const difficultyDist = weeklySummary?.difficultyDistribution || {};
    const cumulativeTags = cumulativeData?.tagTotals || {};

    const thisWeekPosts   = weeklySummary?.totalPosts ?? 0;
    const thisWeekMinutes = weeklySummary?.totalLearningTimeMinutes ?? 0;
    const cumPosts        = cumulativeData?.totalPosts ?? 0;
    const cumMinutes      = cumulativeData?.totalLearningMinutes ?? 0;
    const cumTopCat       = Object.entries(cumulativeData?.categoryDistribution || {}).sort((a, b) => b[1] - a[1])[0]?.[0] || '-';
    const thisWeekTopCat  = Object.entries(categories).sort((a, b) => b[1] - a[1])[0]?.[0] || '-';
    const catSub          = thisWeekTopCat === cumTopCat ? '이번 주도 동일' : `이번 주 ${thisWeekTopCat}`;

    const top3Tags = Object.entries(cumulativeTags).sort((a, b) => b[1] - a[1]).slice(0, 3);

    const hasDifficulty = Object.values(difficultyDist).some((v) => v > 0);
    const hasCategories = Object.keys(categories).length > 0;
    const hasTags       = Object.keys(tags).length > 0 || Object.keys(cumulativeTags).length > 0;

    const personaTitle = parsedAiAnalysis?.weekly_persona?.title;

    return (
        <div className="rd-content">

            {/* 페르소나 배너 */}
            {personaTitle && (
                <div className="rd-persona-banner">
                    이번 주 나는: <strong>{personaTitle}</strong>
                </div>
            )}

            {/* 1행: stat 카드 */}
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

            {/* 규칙 기반 코멘트 — stat 카드 바로 아래 */}
            {ruleBasedComment && (
                <blockquote className="rd-rule-comment">{ruleBasedComment}</blockquote>
            )}

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

            {/* 3행: 기술 스택 막대 + 누적 기준선 */}
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
                {parsedAiAnalysis ? (
                    <AiAnalysisSection ai={parsedAiAnalysis} currentStatus={currentStatus} />
                ) : aiAnalysisComment ? (
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

const AiAnalysisSection = ({ ai, currentStatus }) => {
    const persona   = ai.weekly_persona;
    const tech      = ai.deep_tech_analysis;
    const career    = ai.career_alignment_audit;
    const portfolio = ai.practical_portfolio_advice;
    const roadmap   = ai.next_week_roadmap;
    const cheer     = ai.mentor_cheering_message;

    const careerMeta = CAREER_STATUS_META[career?.status] ?? { label: career?.status, className: 'rd-badge-initial' };
    const portfolioItemLabels = PORTFOLIO_ITEM_LABELS[currentStatus] ?? DEFAULT_PORTFOLIO_LABELS;

    return (
        <div className="rd-ai-sections">

            {/* 총평 */}
            {persona?.total_evaluation && (
                <div className="rd-ai-section rd-ai-section--evaluation">
                    <p className="rd-ai-evaluation">{persona.total_evaluation}</p>
                </div>
            )}

            {/* 기술 집중 분석 */}
            {(tech?.focus_area || tech?.intensity_review) && (
                <div className="rd-ai-section">
                    <span className="rd-ai-section-label">기술 집중 분석</span>
                    <div className="rd-ai-tech-grid">
                        {tech.focus_area && (
                            <div className="rd-ai-tech-item rd-ai-tech-item--blue">
                                <span className="rd-ai-item-title">집중 영역</span>
                                <p>{tech.focus_area}</p>
                            </div>
                        )}
                        {tech.intensity_review && (
                            <div className="rd-ai-tech-item rd-ai-tech-item--purple">
                                <span className="rd-ai-item-title">학습 깊이</span>
                                <p>{tech.intensity_review}</p>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* 커리어 진단 */}
            {career && (
                <div className="rd-ai-section rd-ai-section--career">
                    <span className="rd-ai-section-label">커리어 진단</span>
                    <div className="rd-ai-career-row">
                        <span className={`rd-career-badge ${careerMeta.className}`}>{careerMeta.label}</span>
                        {career.audit_comment && (
                            <p className="rd-ai-career-comment">{career.audit_comment}</p>
                        )}
                    </div>
                </div>
            )}

            {/* 포트폴리오 팁 */}
            {(portfolio?.resume_keyword || portfolio?.interview_question) && (
                <div className="rd-ai-section">
                    <span className="rd-ai-section-label">포트폴리오 팁</span>
                    {portfolio.resume_keyword && (
                        <div className="rd-ai-portfolio-item">
                            <span className="rd-ai-item-title">{portfolioItemLabels.resumeKeyword}</span>
                            <div className="rd-resume-hashtags">
                                {portfolio.resume_keyword.split(',').map((kw) => kw.trim()).filter(Boolean).map((kw) => (
                                    <span key={kw} className="rd-resume-tag">#{kw}</span>
                                ))}
                            </div>
                        </div>
                    )}
                    {portfolio.interview_question && (
                        <div className="rd-ai-portfolio-item">
                            <span className="rd-ai-item-title">{portfolioItemLabels.interviewQuestion}</span>
                            <p className="rd-interview-q">"{portfolio.interview_question}"</p>
                        </div>
                    )}
                </div>
            )}

            {/* 다음 주 로드맵 */}
            {roadmap && (
                <div className="rd-ai-section rd-ai-section--roadmap">
                    <span className="rd-ai-section-label">다음 주 로드맵</span>
                    {roadmap.action_item && (
                        <p className="rd-ai-action">{roadmap.action_item}</p>
                    )}
                    {roadmap.recommended_tech_stacks?.length > 0 && (
                        <>
                        <span className="rd-ai-item-title">추천 기술 스택</span>
                        <div className="rd-roadmap-stack-grid">
                            {roadmap.recommended_tech_stacks.map((s) => (
                                <div key={s.tech_name} className="rd-roadmap-stack-card">
                                    <span className="rd-roadmap-stack-name">{s.tech_name}</span>
                                    {s.reason && <p className="rd-roadmap-stack-reason">{s.reason}</p>}
                                </div>
                            ))}
                        </div>
                        </>
                    )}
                </div>
            )}

            {/* 격려 메시지 */}
            {cheer && (
                <div className="rd-ai-cheer">
                    <span>💌</span>
                    <p>{cheer}</p>
                </div>
            )}

        </div>
    );
};

export default WeeklyReportDetailPage;