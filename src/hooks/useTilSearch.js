import { useState, useEffect, useCallback, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';

const PAGE_SIZE = 10;


// URL 파라미터에서 초기 조건값 파싱 (마운트 시 1회 호출)
function parseConditionsFromUrl(searchParams) {
  return {
    keyword: searchParams.get('keyword') || '',
    nickname: searchParams.get('nickname') || '',
    tagName: searchParams.get('tagName') || '',
    difficulty: searchParams.get('difficulty') || '',
    from: searchParams.get('from') || '',
    to: searchParams.get('to') || '',
    sort: searchParams.get('sort') || 'LATEST',
    page: Number(searchParams.get('page')) || 0,
    advanced: searchParams.get('advanced') === 'true',
  };
}

// URL 쿼리 파라미터 ↔ 검색 상태를 동기화하는 커스텀 훅
// GET /api/tils 호출 담당
export function useTilSearch() {
  const [searchParams, setSearchParams] = useSearchParams();

  // conditions는 컴포넌트 state로 관리, URL 파라미터는 초기값으로만 사용
  const [conditions, setConditions] = useState(() => parseConditionsFromUrl(searchParams));

  const [result, setResult] = useState({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: PAGE_SIZE,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // URL 동기화 — conditions 변경 시 URL에 반영
  // setSearchParams가 re-render를 유발하므로 conditions가 실제로 바뀔 때만 실행
  const prevConditionsRef = useRef(conditions);
  useEffect(() => {
    const prev = prevConditionsRef.current;
    const changed =
        prev.keyword !== conditions.keyword ||
        prev.nickname !== conditions.nickname ||
        prev.tagName !== conditions.tagName ||
        prev.difficulty !== conditions.difficulty ||
        prev.from !== conditions.from ||
        prev.to !== conditions.to ||
        prev.sort !== conditions.sort ||
        prev.page !== conditions.page ||
        prev.advanced !== conditions.advanced;

    if (!changed) return;
    prevConditionsRef.current = conditions;

    const params = {};
    if (conditions.keyword) params.keyword = conditions.keyword;
    if (conditions.nickname) params.nickname = conditions.nickname;
    if (conditions.tagName) params.tagName = conditions.tagName;
    if (conditions.difficulty) params.difficulty = conditions.difficulty;
    if (conditions.from) params.from = conditions.from;
    if (conditions.to) params.to = conditions.to;
    if (conditions.sort !== 'LATEST') params.sort = conditions.sort;
    if (conditions.page > 0) params.page = conditions.page;
    if (conditions.advanced) params.advanced = 'true';

    setSearchParams(params, { replace: true });
  }, [conditions, setSearchParams]);

  // 검색 조건 변경 → API 재호출
  useEffect(() => {
    const fetchTils = async () => {
      setLoading(true);
      setError(null);

      // 목 데이터 모드 (USE_MOCK=true 시 사용)
      // if (USE_MOCK) {
      //   await new Promise(r => setTimeout(r, 300)); // 네트워크 딜레이 시뮬레이션
      //   setResult(MOCK_RESULT);
      //   setLoading(false);
      //   return;
      // }

      try {
        console.log('[useTilSearch] API 호출:', conditions);
        const { data } = await axios.get('/api/tils', {
          params: {
            keyword: conditions.keyword || undefined,
            nickname: conditions.nickname || undefined,
            tagName: conditions.tagName || undefined,
            difficulty: conditions.difficulty || undefined,
            from: conditions.from || undefined,
            to: conditions.to || undefined,
            sort: conditions.sort,
            page: conditions.page,
            size: PAGE_SIZE,
          },
        });
        console.log('[useTilSearch] 응답:', data);
        setResult(data);
      } catch (err) {
        console.error('[useTilSearch] 오류:', err);
        const message = err.response?.data?.message
            || (err.code === 'ERR_NETWORK' ? '백엔드 서버에 연결할 수 없습니다. (USE_MOCK=true 로 테스트 가능)' : '검색 중 오류가 발생했습니다.');
        setError(message);
      } finally {
        setLoading(false);
      }
    };

    fetchTils();
  }, [
    conditions.keyword,
    conditions.nickname,
    conditions.tagName,
    conditions.difficulty,
    conditions.from,
    conditions.to,
    conditions.sort,
    conditions.page,
  ]);

  const setCondition = useCallback((key, value) => {
    setConditions(prev => ({
      ...prev,
      [key]: value,
      page: key === 'page' ? value : 0,
    }));
  }, []);

  const toggleAdvanced = useCallback(() => {
    setConditions(prev => ({ ...prev, advanced: !prev.advanced }));
  }, []);

  const resetConditions = useCallback(() => {
    setConditions({
      keyword: '',
      nickname: '',
      tagName: '',
      difficulty: '',
      from: '',
      to: '',
      sort: 'LATEST',
      page: 0,
      advanced: true,
    });
  }, []);

  const goToPage = useCallback((page) => {
    setConditions(prev => ({ ...prev, page }));
  }, []);

  return {
    conditions,
    result,
    loading,
    error,
    setCondition,
    toggleAdvanced,
    resetConditions,
    goToPage,
  };
}