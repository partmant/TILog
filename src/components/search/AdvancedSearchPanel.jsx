import Select from 'react-select';
import { useTagOptions } from '../../hooks/useTagOptions';

const DIFFICULTIES = ['EASY', 'NORMAL', 'HARD'];

const styles = {
  panel: {
    border: '1px solid #e5e7eb',
    borderRadius: 8,
    padding: '16px 20px',
    marginBottom: 12,
    background: '#f9fafb',
  },
  row: { display: 'flex', gap: 16, alignItems: 'center', marginBottom: 12, flexWrap: 'wrap' },
  label: { minWidth: 60, fontWeight: 500, fontSize: 14, color: '#374151' },
  input: { padding: '6px 10px', border: '1px solid #ccc', borderRadius: 6, fontSize: 14 },
  radioGroup: { display: 'flex', gap: 12, alignItems: 'center' },
  radioLabel: { fontSize: 14, cursor: 'pointer' },
  resetBtn: {
    marginTop: 4,
    padding: '6px 14px',
    border: '1px solid #d1d5db',
    borderRadius: 6,
    background: '#fff',
    cursor: 'pointer',
    fontSize: 14,
  },
};

// 상세검색 패널 — 닉네임 / 태그 / 난이도 / 기간 입력
// props:
//   conditions  object       — { nickname, tagName, difficulty, from, to }
//   onChange    (key, value) => void
//   onReset     () => void
const AdvancedSearchPanel = ({ conditions, onChange, onReset }) => {
  const { tagOptions, loading: tagsLoading } = useTagOptions();
  const selectedTag = tagOptions.find(o => o.value === conditions.tagName) || null;

  return (
    <div style={styles.panel}>
      {/* 닉네임 */}
      <div style={styles.row}>
        <span style={styles.label}>작성자</span>
        <input
          style={styles.input}
          type="text"
          placeholder="닉네임"
          value={conditions.nickname}
          onChange={(e) => onChange('nickname', e.target.value)}
        />
      </div>

      {/* 태그 — react-select 단일 선택 (API가 tagName 단일값) */}
      <div style={styles.row}>
        <span style={styles.label}>태그</span>
        <div style={{ width: 220 }}>
          <Select
            options={tagOptions}
            value={selectedTag}
            onChange={(opt) => onChange('tagName', opt ? opt.value : '')}
            isClearable
            isLoading={tagsLoading}
            placeholder="기술 스택 선택"
          />
        </div>
      </div>

      {/* 난이도 */}
      <div style={styles.row}>
        <span style={styles.label}>난이도</span>
        <div style={styles.radioGroup}>
          <label style={styles.radioLabel}>
            <input
              type="radio"
              value=""
              checked={conditions.difficulty === ''}
              onChange={() => onChange('difficulty', '')}
            />{' '}
            전체
          </label>
          {DIFFICULTIES.map(d => (
            <label key={d} style={styles.radioLabel}>
              <input
                type="radio"
                value={d}
                checked={conditions.difficulty === d}
                onChange={() => onChange('difficulty', d)}
              />{' '}
              {d}
            </label>
          ))}
        </div>
      </div>

      {/* 기간 */}
      <div style={styles.row}>
        <span style={styles.label}>기간</span>
        <input
          style={styles.input}
          type="date"
          value={conditions.from}
          onChange={(e) => onChange('from', e.target.value)}
        />
        <span>~</span>
        <input
          style={styles.input}
          type="date"
          value={conditions.to}
          onChange={(e) => onChange('to', e.target.value)}
        />
      </div>

      <button style={styles.resetBtn} onClick={onReset}>
        초기화
      </button>
    </div>
  );
};

export default AdvancedSearchPanel;