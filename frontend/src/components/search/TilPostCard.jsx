const DIFFICULTY_COLOR = { EASY: '#10b981', NORMAL: '#f59e0b', HARD: '#ef4444' };

const styles = {
  card: {
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    padding: '16px 20px',
    background: '#fff',
    cursor: 'pointer',
    transition: 'box-shadow 0.15s',
  },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 },
  title: { fontWeight: 600, fontSize: 16, color: '#111827', margin: 0 },
  badge: (difficulty) => ({
    fontSize: 12,
    fontWeight: 600,
    color: '#fff',
    background: DIFFICULTY_COLOR[difficulty] || '#9ca3af',
    borderRadius: 4,
    padding: '2px 8px',
  }),
  content: { fontSize: 14, color: '#6b7280', marginBottom: 10, lineHeight: 1.5,
             overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' },
  footer: { display: 'flex', gap: 12, fontSize: 13, color: '#9ca3af' },
};

// TIL 게시글 카드 1개
// TilPostSummaryDto 응답 구조:
//   postId, title, content, authorNickname, difficulty, createdAt, likeCount, commentCount
//
// TODO: 팀원 게시글 상세 페이지가 생기면 onClick에 navigate(`/tils/${post.postId}`) 연결
const TilPostCard = ({ post }) => {
  const { title, content, authorNickname, difficulty, createdAt } = post;
  const date = createdAt ? new Date(createdAt).toLocaleDateString('ko-KR') : '';

  return (
    <div style={styles.card}>
      <div style={styles.header}>
        <h3 style={styles.title}>{title}</h3>
        <span style={styles.badge(difficulty)}>{difficulty}</span>
      </div>
      <p style={styles.content}>{content}</p>
      <div style={styles.footer}>
        <span>@{authorNickname}</span>
        <span>{date}</span>
        {/* likeCount / commentCount — TilPostSummaryDto에 해당 필드가 있으면 표시 */}
        {post.likeCount != null && <span>♥ {post.likeCount}</span>}
        {post.commentCount != null && <span>💬 {post.commentCount}</span>}
      </div>
    </div>
  );
};

export default TilPostCard;