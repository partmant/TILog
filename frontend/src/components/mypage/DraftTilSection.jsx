import { useNavigate } from 'react-router-dom';
import { useMyDrafts } from '../../hooks/mypage/useMyDrafts';
import { formatDateText } from '../../utils/mypageUtils';
import '../../styles/mypage/DraftTilSection.css';

const DIFFICULTY_COLOR = {
    EASY: '#10b981',
    NORMAL: '#f59e0b',
    HARD: '#ef4444',
};

// 휴지통 아이콘
const TrashIcon = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="3 6 5 6 21 6" />
        <path d="M19 6l-1 14H6L5 6" />
        <path d="M10 11v6M14 11v6" />
        <path d="M9 6V4h6v2" />
    </svg>
);

const DraftTilSection = () => {
    const navigate = useNavigate();
    const { drafts, isLoading, error, handleDeleteDraft } = useMyDrafts();

    const handleContinueEditing = (postId) => {
        navigate(`/posts/${postId}/edit`);
    };

    const handleMoveAll = () => {
        navigate('/mypage/drafts');
    };

    return (
        <section className="mypage-panel">
            <div className="mypage-panel-header">
                <div>
                    <h2>임시저장함</h2>
                    <p>이어서 작성할 수 있는 임시저장 TIL입니다.</p>
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
                <div className="draft-til-loading">임시저장 목록을 불러오는 중입니다.</div>
            ) : error ? (
                <div className="draft-til-error">{error}</div>
            ) : drafts.length === 0 ? (
                <div className="draft-til-empty">
                    임시저장된 TIL이 없습니다.<br />
                    글쓰기에서 임시 저장을 누르면 여기서 이어서 작성할 수 있어요.
                </div>
            ) : (
                <div className="draft-til-list">
                    {drafts.slice(0, 5).map((draft) => (
                        <article
                            key={draft.postId}
                            className="draft-til-item"
                            onClick={() => handleContinueEditing(draft.postId)}
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
                                    background: DIFFICULTY_COLOR[draft.difficulty] || '#9ca3af',
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis',
                                    whiteSpace: 'nowrap',
                                }}
                            >
                                {draft.difficulty || 'TIL'}
                            </span>

                            {/* 본문 정보 */}
                            <div className="draft-til-content">
                                <strong>{draft.title || '제목 없음'}</strong>
                                <p>
                                    {draft.studyTime ? `학습시간 ${draft.studyTime}분 · ` : ''}
                                    {formatDateText(draft.updatedAt)} 수정
                                </p>
                            </div>

                            {/* 이어서 작성 안내 + 삭제 버튼 */}
                            <div className="draft-til-arrow" onClick={(e) => e.stopPropagation()}>
                                <span>이어서 작성</span>
                                <button
                                    type="button"
                                    className="draft-til-delete-btn"
                                    aria-label="임시저장 삭제"
                                    title="임시저장 삭제"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        if (window.confirm('이 임시저장 TIL을 삭제할까요?')) {
                                            handleDeleteDraft(draft.postId);
                                        }
                                    }}
                                >
                                    <TrashIcon />
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </section>
    );
};

export default DraftTilSection;
