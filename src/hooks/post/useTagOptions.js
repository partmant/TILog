import { useState, useEffect } from 'react';
import api from '../../api/axios.js';

// 앱 생명주기 동안 1회만 fetch — 이후 호출은 이 캐시 반환
let cache = null;

// GET /api/tags 결과를 react-select 옵션 형태로 반환하는 커스텀 훅
// { tagOptions: [{value, label}, ...], loading: bool }
export function useTagOptions() {
  const [tagOptions, setTagOptions] = useState(cache);
  const [loading, setLoading] = useState(!cache);

  useEffect(() => {
    if (cache) return;

    api.get('/api/tags')
      .then(({ data }) => {
        cache = data.map(name => ({ value: name, label: name }));
        setTagOptions(cache);
      })
      .catch(() => {
        // 실패 시 빈 목록 — Select가 옵션 없이 렌더링됨
        cache = [];
        setTagOptions(cache);
      })
      .finally(() => setLoading(false));
  }, []);

  return { tagOptions: tagOptions ?? [], loading };
}