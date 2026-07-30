const SORT_OPTIONS = [
  { value: 'LATEST', label: '최신순' },
  { value: 'LIKES', label: '좋아요순' },
  { value: 'COMMENTS', label: '댓글순' },
];

const styles = {
  wrapper: { display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 },
  select: { padding: '6px 10px', border: '1px solid #ccc', borderRadius: 6, fontSize: 14 },
  total: { marginLeft: 'auto', fontSize: 14, color: '#6b7280' },
};

// 정렬 드롭다운 + 총 게시글 수 표시
// props:
//   sort          string — 현재 정렬값 ('LATEST' | 'LIKES' | 'COMMENTS')
//   totalElements number — 검색 결과 총 건수
//   onChange      (sort) => void
const SortSelector = ({ sort, totalElements, onChange }) => (
  <div style={styles.wrapper}>
    <select style={styles.select} value={sort} onChange={(e) => onChange(e.target.value)}>
      {SORT_OPTIONS.map(o => (
        <option key={o.value} value={o.value}>{o.label}</option>
      ))}
    </select>
    <span style={styles.total}>총 {totalElements.toLocaleString()}건</span>
  </div>
);

export default SortSelector;