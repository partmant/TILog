const styles = {
  wrapper: { display: 'flex', justifyContent: 'center', gap: 4 },
  btn: (active) => ({
    padding: '6px 12px',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    background: active ? '#3b82f6' : '#fff',
    color: active ? '#fff' : '#374151',
    cursor: 'pointer',
    fontWeight: active ? 600 : 400,
  }),
};

// 페이지 네비게이션
// props:
//   currentPage  number — 현재 페이지 (0-based)
//   totalPages   number
//   onPageChange (page) => void
const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  // 최대 5개 페이지 버튼 노출 (현재 페이지 중심)
  const range = 2;
  const start = Math.max(0, currentPage - range);
  const end = Math.min(totalPages - 1, currentPage + range);
  const pages = [];
  for (let i = start; i <= end; i++) pages.push(i);

  return (
    <div style={styles.wrapper}>
      <button
        style={styles.btn(false)}
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        &lt;
      </button>

      {start > 0 && (
        <>
          <button style={styles.btn(false)} onClick={() => onPageChange(0)}>1</button>
          {start > 1 && <span style={{ padding: '6px 4px', color: '#9ca3af' }}>…</span>}
        </>
      )}

      {pages.map(p => (
        <button
          key={p}
          style={styles.btn(p === currentPage)}
          onClick={() => onPageChange(p)}
        >
          {p + 1}
        </button>
      ))}

      {end < totalPages - 1 && (
        <>
          {end < totalPages - 2 && <span style={{ padding: '6px 4px', color: '#9ca3af' }}>…</span>}
          <button style={styles.btn(false)} onClick={() => onPageChange(totalPages - 1)}>
            {totalPages}
          </button>
        </>
      )}

      <button
        style={styles.btn(false)}
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        &gt;
      </button>
    </div>
  );
};

export default Pagination;