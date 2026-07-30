import { useRef, useState } from 'react';
import Select from 'react-select';
import { useTagOptions } from '../../hooks/post/useTagOptions.js';

// 상세검색 패널 — 닉네임 / 태그 / 기간
// props:
//   conditions   object            — { nickname, tagName, from, to }
//   onChange     (key, value) => void
//   onReset      () => void
//   showNickname boolean           — 닉네임 입력란 표시 여부 (기본 true)
const AdvancedSearchPanel = ({ conditions, onChange, onReset, showNickname = true }) => {
  const { tagOptions, loading: tagsLoading } = useTagOptions();
  const selectedTag = tagOptions.find(o => o.value === conditions.tagName) || null;

  // 한국어 IME 조합 중 onChange 중복 호출 방지
  const [nicknameInput, setNicknameInput] = useState(conditions.nickname ?? '');
  const isComposing = useRef(false);

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-xl">

      {/* 닉네임 — showNickname=false 이면 숨김 */}
      {showNickname && (
        <div className="mb-4 flex items-center gap-3">
          <span className="w-12 shrink-0 text-sm font-bold text-gray-500">작성자</span>
          <input
            type="text"
            className="flex-1 rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none transition focus:border-purple-400 focus:ring-2 focus:ring-purple-100"
            placeholder="닉네임"
            value={nicknameInput}
            onChange={(e) => {
              setNicknameInput(e.target.value);
              if (!isComposing.current) onChange('nickname', e.target.value);
            }}
            onCompositionStart={() => { isComposing.current = true; }}
            onCompositionEnd={(e) => {
              isComposing.current = false;
              onChange('nickname', e.target.value);
            }}
          />
        </div>
      )}

      {/* 태그 */}
      <div className="mb-4 flex items-center gap-3">
        <span className="w-12 shrink-0 text-sm font-bold text-gray-500">태그</span>
        <div className="w-52">
          <Select
            options={tagOptions}
            value={selectedTag}
            onChange={(opt) => onChange('tagName', opt ? opt.value : '')}
            isClearable
            isLoading={tagsLoading}
            placeholder="기술 스택 선택"
            styles={{
              control: (base, state) => ({
                ...base,
                borderRadius: '0.75rem',
                borderColor: state.isFocused ? '#a78bfa' : '#e5e7eb',
                boxShadow: state.isFocused ? '0 0 0 2px #ede9fe' : 'none',
                fontSize: '0.875rem',
                '&:hover': { borderColor: '#c4b5fd' },
              }),
              option: (base, state) => ({
                ...base,
                fontSize: '0.875rem',
                backgroundColor: state.isSelected ? '#8b5cf6' : state.isFocused ? '#f5f3ff' : 'white',
              }),
            }}
          />
        </div>
      </div>

      {/* 기간 */}
      <div className="mb-5 flex items-center gap-3">
        <span className="w-12 shrink-0 text-sm font-bold text-gray-500">기간</span>
        <input
          type="date"
          className="rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none transition focus:border-purple-400"
          value={conditions.from}
          onChange={(e) => onChange('from', e.target.value)}
        />
        <span className="text-gray-400">~</span>
        <input
          type="date"
          className="rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none transition focus:border-purple-400"
          value={conditions.to}
          onChange={(e) => onChange('to', e.target.value)}
        />
      </div>

      <button
        type="button"
        className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-2 text-sm font-bold text-gray-600 transition hover:bg-gray-100"
        onClick={onReset}
      >
        초기화
      </button>
    </div>
  );
};

export default AdvancedSearchPanel;