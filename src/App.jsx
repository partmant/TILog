import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import MyPage from "./pages/MyPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import { isLoggedIn } from "./utils/authUtils";

// 로그인 필요 라우트 - 미인증 시 /login으로 리다이렉트
const PrivateRoute = ({ children }) => {
    return isLoggedIn() ? children : <Navigate to="/login" replace />;
};

const App = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route
                    element={
                        <PrivateRoute>
                            <MainLayout />
                        </PrivateRoute>
                    }
                >
                    <Route path="/mypage" element={<MyPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
};

export default App;
