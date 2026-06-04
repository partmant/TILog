import { useMemo } from 'react';
import {
    buildMonthlyHeatmap,
    getHeatLevel,
} from '../../utils/mypageUtils';
import '../../styles/mypage/HeatmapSection.css';

const WEEK_DAY_LABELS = ['일', '월', '화', '수', '목', '금', '토'];

const HeatmapSection = ({
                            heatmapDays,
                            selectedMonthCount,
                            onChangeMonthCount,
                            isLoading,
                        }) => {
    const monthlyHeatmap = useMemo(
        () => buildMonthlyHeatmap(heatmapDays),
        [heatmapDays]
    );

    const totalWriteCount = heatmapDays.reduce((sum, day) => sum + day.writeCount, 0);
    const writtenDays = heatmapDays.filter((day) => day.writeCount > 0).length;
    const averageWriteCount = writtenDays === 0 ? 0 : totalWriteCount / writtenDays;

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

                        <div className="mypage-month-heatmap-scroll">
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
                                                                title={`${day.date} · ${day.writeCount}개 작성`}
                                                                aria-label={`${day.date}에 ${day.writeCount}개 작성`}
                                                            >
                                <span className="mypage-heatmap-tooltip">
                                  {day.date}
                                    <br />
                                    {day.writeCount}개 작성
                                </span>
                              </span>
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
        </section>
    );
};

export default HeatmapSection;
