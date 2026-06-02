import { useNavigate } from 'react-router-dom';
import {
    getTilCategory,
    getTilDate,
    getTilMeta,
    getTilTitle,
} from '../../utils/mypageUtils';
import '../../styles/mypage/RecentTilSection.css';

const RecentTilSection = ({ recentTils, isLoading }) => {
    const navigate = useNavigate();

    return (
        <section className="mypage-panel mypage-recent-panel">
            <div className="mypage-panel-header">
                <div>
                    <h2>최근 작성한 TIL</h2>
                    <p>최신순으로 작성 기록을 확인합니다.</p>
                </div>

                <button
                    className="mypage-text-button"
                    type="button"
                    onClick={() => navigate('/tils')}
                >
                    전체 보기
                </button>
            </div>

            {isLoading ? (
                <div className="mypage-recent-loading">
                    최근 TIL 목록을 불러오는 중입니다.
                </div>
            ) : (
                <div className="mypage-recent-list">
                    {recentTils.length === 0 ? (
                        <div className="mypage-empty">최근 작성한 TIL이 없습니다.</div>
                    ) : (
                        recentTils.map((til, index) => (
                            <article
                                className="mypage-recent-item"
                                key={til.postId ?? til.id ?? index}
                                onClick={() => navigate(`/tils/${til.postId ?? til.id}`)}
                            >
                                <span className={`mypage-category category-${index % 4}`}>
                                    {getTilCategory(til)}
                                </span>

                                <div className="mypage-recent-content">
                                    <strong>{getTilTitle(til)}</strong>
                                    <p>{getTilMeta(til)}</p>
                                </div>

                                <div className="mypage-recent-date">
                                    <span>{getTilDate(til)}</span>
                                    <b>›</b>
                                </div>
                            </article>
                        ))
                    )}
                </div>
            )}
        </section>
    );
};

export default RecentTilSection;
