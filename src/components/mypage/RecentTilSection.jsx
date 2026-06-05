import { useNavigate } from 'react-router-dom';
import {
    getTilCategory,
    getTilDate,
    getTilMeta,
    getTilTitle,
} from '../../utils/mypageUtils';
import '../../styles/mypage/RecentTilSection.css';

const getTilPostId = (til) => {
    return til.postId ?? til.id ?? til.tilId;
};

const RecentTilSection = ({
                              recentTils,
                              isLoading,
                              memberId,
                          }) => {
    const navigate = useNavigate();

    const handleMoveAllTils = () => {
        if (!memberId) {
            navigate('/login');
            return;
        }

        navigate(`/members/${memberId}/tils`);
    };

    const handleMoveDetail = (til) => {
        const postId = getTilPostId(til);

        if (!postId) {
            alert('게시글 정보를 찾을 수 없습니다.');
            return;
        }

        navigate(`/posts/${postId}`);
    };

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
                    onClick={handleMoveAllTils}
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
                        recentTils.map((til, index) => {
                            const postId = getTilPostId(til);

                            return (
                                <article
                                    className="mypage-recent-item"
                                    key={postId ?? index}
                                    onClick={() => handleMoveDetail(til)}
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
                            );
                        })
                    )}
                </div>
            )}
        </section>
    );
};

export default RecentTilSection;
