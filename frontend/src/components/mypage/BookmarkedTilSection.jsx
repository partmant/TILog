import { useNavigate } from 'react-router-dom';
import { useMyBookmarkedTils } from '../../hooks/mypage/useMyBookmarkedTils';
import { useBookmark } from '../../hooks/post/useBookmark';
import { useToast } from '../../hooks/useToast';
import { getTilDate } from '../../utils/mypageUtils';
import '../../styles/mypage/BookmarkedTilSection.css';
import Toast from '../common/Toast.jsx';

const DIFFICULTY_COLOR = {
    EASY: '#10b981',
    NORMAL: '#f59e0b',
    HARD: '#ef4444',
};

const BookmarkedTilSection = () => {
    const navigate = useNavigate();
    const { posts, isLoading, error, handleBookmarkChange } = useMyBookmarkedTils();
    const { toast, showToast } = useToast();
    const { handleToggleBookmark, loadingPostId } = useBookmark(handleBookmarkChange, showToast);

    const handleMoveDetail = (postId) => {
        navigate(`/posts/${postId}`);
    };

    const handleMoveAll = () => {
        navigate('/mypage/bookmarks');
    };

    return (
        <>
        <Toast toast={toast} />
        <section className="mypage-panel">
            <div className="mypage-panel-header">
                <div>
                    <h2>즐겨찾기한 TIL</h2>
                    <p>나중에 다시 보고 싶은 TIL 목록입니다.</p>
                </div>
                <button
                    className="mypage-text-button"
                    type="button"
                    onClick={handleMoveAll}
                >
                    전체 보기
                </button>
            </div>

            {isLoading ? (
                <div className="bookmarked-til-loading">즐겨찾기 목록을 불러오는 중입니다.</div>
            ) : error ? (
                <div className="bookmarked-til-error">{error}</div>
            ) : posts.length === 0 ? (
                <div className="bookmarked-til-empty">
                    아직 즐겨찾기한 TIL이 없습니다.<br />
                    나중에 다시 보고 싶은 TIL을 목록에서 즐겨찾기해보세요.
                </div>
            ) : (
                <div className="bookmarked-til-list">
                    {posts.map((post) => (
                        <article
                            key={post.postId}
                            className="bookmarked-til-item"
                            onClick={() => handleMoveDetail(post.postId)}
                        >
                            {/* 난이도 배지 */}
                            <span
                                style={{
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    minWidth: '78px',
                                    maxWidth: '94px',
                                    height: '34px',
                                    padding: '0 12px',
                                    borderRadius: '999px',
                                    color: '#ffffff',
                                    fontSize: '12px',
                                    fontWeight: 900,
                                    background: DIFFICULTY_COLOR[post.difficulty] || '#9ca3af',
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis',
                                    whiteSpace: 'nowrap',
                                }}
                            >
                                {post.difficulty || 'TIL'}
                            </span>

                            {/* 본문 정보 */}
                            <div className="bookmarked-til-content">
                                <strong>{post.title}</strong>
                                <p>
                                    {post.nickname} · 댓글 {post.commentCount ?? 0} · 좋아요 {post.likeCount ?? 0}
                                    {post.studyTime ? ` · ${post.studyTime}분` : ''}
                                </p>
                                {(post.tagNames || []).length > 0 && (
                                    <div className="bookmarked-til-tags">
                                        {post.tagNames.slice(0, 3).map((tag) => (
                                            <span key={tag} className="bookmarked-til-tag">#{tag}</span>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {/* 날짜 + 즐겨찾기 해제 버튼 */}
                            <div className="bookmarked-til-arrow" onClick={(e) => e.stopPropagation()}>
                                <span>{getTilDate(post)}</span>
                                <button
                                    type="button"
                                    disabled={loadingPostId === post.postId}
                                    aria-label="즐겨찾기 해제"
                                    title="즐겨찾기 해제"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handleToggleBookmark(post.postId, true);
                                    }}
                                    style={{
                                        background: 'none',
                                        border: 'none',
                                        cursor: loadingPostId === post.postId ? 'not-allowed' : 'pointer',
                                        padding: '4px',
                                        borderRadius: '50%',
                                        display: 'flex',
                                        alignItems: 'center',
                                        opacity: loadingPostId === post.postId ? 0.5 : 1,
                                    }}
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="#f59e0b">
                                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                    </svg>
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </section>
        </>
    );
};

export default BookmarkedTilSection;
