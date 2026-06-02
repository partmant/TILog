import { NavLink, useNavigate } from "react-router-dom";
import "../../styles/common/Header.css";

const Header = () => {
    const navigate = useNavigate();

    return (
        <header className="app-header">
            <button
                type="button"
                className="app-header-brand"
                onClick={() => navigate("/mypage")}
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
                    to="/tils"
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

                <NavLink
                    to="/mypage"
                    className={({ isActive }) =>
                        isActive ? "app-header-nav-link active" : "app-header-nav-link"
                    }
                >
                    마이페이지
                </NavLink>
            </nav>

            <div className="app-header-actions">
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
                    U
                </button>
            </div>
        </header>
    );
};

export default Header;
