import { useState } from 'react';

const styles = {
  wrapper: { display: 'flex', gap: 8, alignItems: 'center', marginBottom: 8 },
  input: { flex: 1, padding: '8px 12px', fontSize: 15, border: '1px solid #ccc', borderRadius: 6 },
  btn: { padding: '8px 16px', cursor: 'pointer', borderRadius: 6, border: '1px solid #ccc' },
  searchBtn: { background: '#3b82f6', color: '#fff', border: 'none' },
};

// 키워드 입력창 + 검색 버튼 + 상세검색 토글
// props:
//   keyword       string   — 현재 키워드 값
//   advanced      boolean  — 상세검색 패널 열림 여부
//   onSearch      (keyword) => void  — 검색 실행 (Enter 또는 버튼)
//   onToggleAdvanced () => void      — 상세검색 토글
const SearchBar = ({ keyword, advanced, onSearch, onToggleAdvanced }) => {
  // 로컬 입력 상태 (Enter/버튼 클릭 시에만 실제 검색 실행)
  const [input, setInput] = useState(keyword);

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') onSearch(input);
  };

  return (
    <div style={styles.wrapper}>
      <input
        style={styles.input}
        type="text"
        placeholder="제목 또는 본문 키워드 검색"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyDown={handleKeyDown}
      />
      <button style={{ ...styles.btn, ...styles.searchBtn }} onClick={() => onSearch(input)}>
        검색
      </button>
      <button style={styles.btn} onClick={onToggleAdvanced}>
        상세검색 {advanced ? '▲' : '▼'}
      </button>
    </div>
  );
};

export default SearchBar;