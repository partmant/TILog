import TilPostCard from './TilPostCard';

const styles = {
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
    gap: 16,
    marginBottom: 24,
  },
  empty: { textAlign: 'center', color: '#9ca3af', padding: '48px 0', fontSize: 15 },
  loading: { textAlign: 'center', color: '#6b7280', padding: '48px 0' },
  error: { textAlign: 'center', color: '#ef4444', padding: '24px 0' },
};

// 게시글 카드 목록
// props:
//   posts   TilPostSummaryDto[]
//   loading boolean
//   error   string | null
const TilPostList = ({ posts, loading, error }) => {
  if (loading) return <div style={styles.loading}>검색 중...</div>;
  if (error) return <div style={styles.error}>{error}</div>;
  if (!posts.length) return <div style={styles.empty}>검색 결과가 없습니다.</div>;

  return (
    <div style={styles.grid}>
      {posts.map(post => (
        // TilPostSummaryDto의 PK 필드명 — 백엔드 응답에 맞게 확인 필요 (postId 또는 id)
        <TilPostCard key={post.postId ?? post.id} post={post} />
      ))}
    </div>
  );
};

export default TilPostList;