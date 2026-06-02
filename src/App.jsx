import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import MyPage from "./pages/MyPage";

const App = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Navigate to="/mypage" replace />} />
                    <Route path="/mypage" element={<MyPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
};

export default App;
