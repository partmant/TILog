import { Navigate } from "react-router-dom";
import { isLoggedIn } from "../utils/authUtils";

// 로그인 필요 라우트
// 미인증 사용자는 로그인 페이지로 이동
const PrivateRoute = ({ children }) => {
    return isLoggedIn() ? children : <Navigate to="/login" replace />;
};

export default PrivateRoute;