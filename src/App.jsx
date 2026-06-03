import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import MyPage from "./pages/MyPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";

const App = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Navigate to="/mypage" replace />} />
                    <Route path="/mypage" element={<MyPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
};

export default App;
