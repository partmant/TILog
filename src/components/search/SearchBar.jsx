import { useState } from 'react';

// 키워드 입력창 + 검색 버튼 + 상세검색 토글
// props:
//   keyword           string   — 현재 키워드 값
//   advanced          boolean  — 상세검색 패널 열림 여부
//   onSearch          (keyword) => void
//   onToggleAdvanced  () => void
const SearchBar = ({ keyword, advanced, onSearch, onToggleAdvanced }) => {
  const [input, setInput] = useState(keyword ?? '');

  return (
    <div className="flex items-center gap-2">
      <input
        type="text"
        className="w-64 rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm outline-none transition focus:border-purple-400 focus:ring-2 focus:ring-purple-100"
        placeholder="제목 또는 본문 키워드 검색"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && onSearch(input)}
      />
      <button
        type="button"
        className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-5 py-2.5 text-sm font-bold text-white transition hover:opacity-90"
        onClick={() => onSearch(input)}
      >
        검색
      </button>
      <button
        type="button"
        className={`rounded-xl border px-4 py-2.5 text-sm font-bold transition ${
          advanced
            ? 'border-purple-400 bg-purple-50 text-purple-600'
            : 'border-gray-200 bg-white text-gray-600 hover:border-purple-300 hover:text-purple-500'
        }`}
        onClick={onToggleAdvanced}
      >
        상세검색 {advanced ? '▲' : '▼'}
      </button>
    </div>
  );
};

export default SearchBar;