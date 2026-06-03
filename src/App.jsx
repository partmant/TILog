import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import MyPage from "./pages/MyPage";
import TilSearchPage from "./pages/TilSearchPage.jsx";
import WeeklyReportDetailPage from "./pages/WeeklyReportDetailPage.jsx";

const App = () => {
    return (
        <BrowserRouter>

            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Navigate to="/mypage" replace />} />
                    <Route path="/mypage" element={<MyPage />} />
                    <Route path="/search" element={<TilSearchPage />} />
                    <Route path="/weekly-report" element={<WeeklyReportDetailPage />} />

                </Route>
            </Routes>
        </BrowserRouter>
    );
};

export default App;
