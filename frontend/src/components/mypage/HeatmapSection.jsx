import {
    useMemo,
    useState,
} from 'react';
import { createPortal } from 'react-dom';
import {
    buildMonthlyHeatmap,
    getHeatLevel,
} from '../../utils/mypageUtils';
import '../../styles/mypage/HeatmapSection.css';

const WEEK_DAY_LABELS = ['일', '월', '화', '수', '목', '금', '토'];

const TOOLTIP_WIDTH = 132;
const TOOLTIP_HEIGHT = 48;
const TOOLTIP_MARGIN = 12;
const TOOLTIP_OFFSET = 10;

const getTooltipPosition = (cellElement) => {
    const rect = cellElement.getBoundingClientRect();

    const centerX = rect.left + rect.width / 2;
    const minLeft = TOOLTIP_MARGIN + TOOLTIP_WIDTH / 2;
    const maxLeft = window.innerWidth - TOOLTIP_MARGIN - TOOLTIP_WIDTH / 2;

    const left = Math.min(
        Math.max(centerX, minLeft),
        maxLeft
    );

    const bottomTop = rect.bottom + TOOLTIP_OFFSET;
    const isBottomOverflow = bottomTop + TOOLTIP_HEIGHT > window.innerHeight - TOOLTIP_MARGIN;

    if (isBottomOverflow) {
        return {
            left,
            top: rect.top - TOOLTIP_OFFSET,
            placement: 'top',
        };
    }

    return {
        left,
        top: bottomTop,
        placement: 'bottom',
    };
};

const HeatmapSection = ({
                            heatmapDays,
                            selectedMonthCount,
                            onChangeMonthCount,
                            isLoading,
                        }) => {
    const [activeTooltip, setActiveTooltip] = useState(null);

    const monthlyHeatmap = useMemo(
        () => buildMonthlyHeatmap(heatmapDays),
        [heatmapDays]
    );

    const totalWriteCount = heatmapDays.reduce((sum, day) => sum + day.writeCount, 0);
    const writtenDays = heatmapDays.filter((day) => day.writeCount > 0).length;
    const averageWriteCount = writtenDays === 0 ? 0 : totalWriteCount / writtenDays;

    const handleTooltipOpen = (event, day) => {
        const position = getTooltipPosition(event.currentTarget);

        setActiveTooltip({
            date: day.date,
            writeCount: day.writeCount,
            ...position,
        });
    };

    const handleTooltipClose = () => {
        setActiveTooltip(null);
    };

    return (
        <section className="mypage-panel mypage-heatmap-panel">
            <div className="mypage-panel-header">
                <div>
                    <h2>TIL 잔디 히트맵</h2>
                    <p>선택한 기간 동안의 TIL 작성 기록입니다.</p>
                </div>

                <select
                    className="mypage-period-select"
                    value={selectedMonthCount}
                    onChange={(event) => onChangeMonthCount(Number(event.target.value))}
                >
                    <option value={3}>최근 3개월</option>
                    <option value={6}>최근 6개월</option>
                    <option value={12}>최근 12개월</option>
                </select>
            </div>

            {isLoading ? (
                <div className="mypage-heatmap-loading">
                    잔디 기록을 불러오는 중입니다.
                </div>
            ) : (
                <>
                    <div className="mypage-heatmap-layout">
                        <div className="mypage-weekday-column">
                            <div className="mypage-weekday-top-space" />

                            {WEEK_DAY_LABELS.map((label) => (
                                <span key={label}>{label}</span>
                            ))}
                        </div>

                        <div
                            className="mypage-month-heatmap-scroll"
                            onScroll={handleTooltipClose}
                        >
                            <div className="mypage-month-heatmap-list">
                                {monthlyHeatmap.map((monthData) => (
                                    <div className="mypage-month-block" key={monthData.key}>
                                        <div className="mypage-month-title">
                                            {monthData.label}
                                        </div>

                                        <div className="mypage-month-grid">
                                            {monthData.weeks.map((week, weekIndex) => (
                                                <div
                                                    className="mypage-month-week"
                                                    key={`${monthData.key}-week-${weekIndex}`}
                                                >
                                                    {week.map((day, dayIndex) => {
                                                        if (!day) {
                                                            return (
                                                                <span
                                                                    className="mypage-heatmap-cell outside"
                                                                    key={`${monthData.key}-empty-${weekIndex}-${dayIndex}`}
                                                                />
                                                            );
                                                        }

                                                        return (
                                                            <span
                                                                className={`mypage-heatmap-cell level-${getHeatLevel(day.writeCount)}`}
                                                                key={day.date}
                                                                aria-label={`${day.date}에 ${day.writeCount}개 작성`}
                                                                onMouseEnter={(event) => handleTooltipOpen(event, day)}
                                                                onMouseLeave={handleTooltipClose}
                                                                onFocus={(event) => handleTooltipOpen(event, day)}
                                                                onBlur={handleTooltipClose}
                                                                tabIndex={0}
                                                            />
                                                        );
                                                    })}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="mypage-heatmap-bottom">
                        <div className="mypage-legend">
                            <span>Less</span>
                            <i className="level-0" />
                            <i className="level-1" />
                            <i className="level-2" />
                            <i className="level-3" />
                            <i className="level-4" />
                            <span>More</span>
                        </div>

                        <div className="mypage-heatmap-summary">
                            <div>
                                <span>총 작성</span>
                                <strong>{totalWriteCount}개</strong>
                            </div>

                            <div>
                                <span>작성한 날짜</span>
                                <strong>{writtenDays}일</strong>
                            </div>

                            <div>
                                <span>평균 작성</span>
                                <strong>{averageWriteCount.toFixed(1)}개/일</strong>
                            </div>
                        </div>
                    </div>
                </>
            )}

            {activeTooltip && typeof document !== 'undefined' && createPortal(
                <div
                    className={`mypage-heatmap-tooltip-portal ${activeTooltip.placement}`}
                    style={{
                        left: `${activeTooltip.left}px`,
                        top: `${activeTooltip.top}px`,
                    }}
                    role="tooltip"
                >
                    {activeTooltip.date}
                    <br />
                    {activeTooltip.writeCount}개 작성
                </div>,
                document.body
            )}
        </section>
    );
};

export default HeatmapSection;
