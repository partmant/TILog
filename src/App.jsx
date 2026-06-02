import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import MyPage from "./pages/MyPage";
import TilSearchPage from "./pages/TilSearchPage.jsx";

const App = () => {
    return (
        <BrowserRouter>
            {/* 임시 내비게이션 — 팀원 레이아웃/헤더 컴포넌트로 교체 */}
            {/*<nav style={{ padding: '12px 24px', borderBottom: '1px solid #e5e7eb' }}>*/}
            {/*    <Link to="/search" style={{ fontWeight: 600, color: '#3b82f6', textDecoration: 'none' }}>*/}
            {/*        TIL 검색*/}
            {/*    </Link>*/}
            {/*</nav>*/}

            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Navigate to="/mypage" replace />} />
                    <Route path="/mypage" element={<MyPage />} />
                    <Route path="/search" element={<TilSearchPage />} />

                </Route>
            </Routes>
        </BrowserRouter>
    );
};

export default App;
