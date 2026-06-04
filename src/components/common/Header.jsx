import { NavLink, useNavigate } from "react-router-dom";
import { getCurrentUser, isLoggedIn, logout } from "../../utils/authUtils";
import "../../styles/common/Header.css";

const Header = () => {
    const navigate = useNavigate();

    const loggedIn = isLoggedIn();
    const user = getCurrentUser();
    const initial = user?.nickname?.charAt(0).toUpperCase() ?? "U";

    const handleLogout = () => {
        logout();
        navigate("/login", { replace: true });
    };

    return (
        <header className="app-header">
            <button
                type="button"
                className="app-header-brand"
                onClick={() => navigate(loggedIn ? "/mypage" : "/feed")}
            >
                <div className="app-header-logo">T</div>

                <div>
                    <strong>TILog</strong>
                    <span>Growth Platform</span>
                </div>
            </button>

            <nav className="app-header-nav">
                <NavLink
                    to="/feed"
                    className={({ isActive }) =>
                        isActive ? "app-header-nav-link active" : "app-header-nav-link"
                    }
                >
                    메인 피드
                </NavLink>

                <NavLink
                    to="/posts"
                    className={({ isActive }) =>
                        isActive ? "app-header-nav-link active" : "app-header-nav-link"
                    }
                >
                    TIL 목록
                </NavLink>

                <NavLink
                    to="/feedback"
                    className={({ isActive }) =>
                        isActive ? "app-header-nav-link active" : "app-header-nav-link"
                    }
                >
                    피드백
                </NavLink>

                {loggedIn && (
                    <NavLink
                        to="/mypage"
                        className={({ isActive }) =>
                            isActive ? "app-header-nav-link active" : "app-header-nav-link"
                        }
                    >
                        마이페이지
                    </NavLink>
                )}
            </nav>

            <div className="app-header-actions">
                {loggedIn ? (
                    <>
                        <button
                            type="button"
                            className="app-header-write-button"
                            onClick={() => navigate("/posts/write")}
                        >
                            TIL 작성하기
                        </button>

                        <button
                            type="button"
                            className="app-header-profile-button"
                            onClick={() => navigate("/mypage")}
                            aria-label="마이페이지로 이동"
                        >
                            {initial}
                        </button>

                        <button
                            type="button"
                            className="app-header-logout-button"
                            onClick={handleLogout}
                            aria-label="로그아웃"
                        >
                            로그아웃
                        </button>
                    </>
                ) : (
                    <>
                        <button
                            type="button"
                            className="app-header-logout-button"
                            onClick={() => navigate("/login")}
                        >
                            로그인
                        </button>

                        <button
                            type="button"
                            className="app-header-write-button"
                            onClick={() => navigate("/signup")}
                        >
                            회원가입
                        </button>
                    </>
                )}
            </div>
        </header>
    );
};

export default Header;
