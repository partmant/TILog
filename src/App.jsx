import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import TilSearchPage from './pages/TilSearchPage';

// TODO: 팀원 페이지 완성되면 아래에 Route 추가
// import HomePage from './pages/HomePage';
// import TilDetailPage from './pages/TilDetailPage';

const App = () => {
  return (
    <BrowserRouter>
      {/* 임시 내비게이션 — 팀원 레이아웃/헤더 컴포넌트로 교체 */}
      <nav style={{ padding: '12px 24px', borderBottom: '1px solid #e5e7eb' }}>
        <Link to="/search" style={{ fontWeight: 600, color: '#3b82f6', textDecoration: 'none' }}>
          TIL 검색
        </Link>
      </nav>

      <Routes>
        {/* 팀원 홈 라우트 생기면 여기 추가 */}
        <Route path="/search" element={<TilSearchPage />} />

        {/* 임시 루트 — 팀원 홈 페이지 연결 전까지 검색 페이지로 리다이렉트 */}
        <Route path="/" element={<TilSearchPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;