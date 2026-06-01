import { useTilSearch } from '../hooks/useTilSearch';
import SearchBar from '../components/search/SearchBar';
import AdvancedSearchPanel from '../components/search/AdvancedSearchPanel';
import SortSelector from '../components/search/SortSelector';
import TilPostList from '../components/search/TilPostList';
import Pagination from '../components/search/Pagination';

const styles = {
  page: { maxWidth: 1100, margin: '0 auto', padding: '32px 16px' },
  title: { fontSize: 24, fontWeight: 700, marginBottom: 20, color: '#111827' },
};

// TIL 검색 페이지 — 검색/정렬/페이징 기능 진입점
// App.jsx에서 <Route path="/search" element={<TilSearchPage />} /> 로 연결
const TilSearchPage = () => {
  const {
    conditions,
    result,
    loading,
    error,
    setCondition,
    toggleAdvanced,
    resetConditions,
  } = useTilSearch();

  return (
    <div style={styles.page}>
      <h1 style={styles.title}>TIL 검색</h1>

      {/* 키워드 검색바 + 상세검색 토글 */}
      <SearchBar
        keyword={conditions.keyword}
        advanced={conditions.advanced}
        onSearch={(keyword) => setCondition('keyword', keyword)}
        onToggleAdvanced={toggleAdvanced}
      />

      {/* 상세검색 패널 (advanced=true일 때만 표시) */}
      {conditions.advanced && (
        <AdvancedSearchPanel
          conditions={conditions}
          onChange={setCondition}
          onReset={resetConditions}
        />
      )}

      {/* 정렬 드롭다운 + 총 건수 */}
      <SortSelector
        sort={conditions.sort}
        totalElements={result.totalElements}
        onChange={(sort) => setCondition('sort', sort)}
      />

      {/* 게시글 목록 */}
      <TilPostList
        posts={result.content}
        loading={loading}
        error={error}
      />

      {/* 페이지네이션 */}
      <Pagination
        currentPage={result.number}
        totalPages={result.totalPages}
        onPageChange={(page) => setCondition('page', page)}
      />
    </div>
  );
};

export default TilSearchPage;