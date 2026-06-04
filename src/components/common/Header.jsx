import { useState, useEffect, useRef } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { getCurrentUser, isLoggedIn, logout } from "../../utils/authUtils";
import { useSSE } from "../../hooks/sse/useSSE";
import "../../styles/common/Header.css";
import { fetchMyNotifications, markNotificationAsRead, markAllNotificationsAsRead } from "../../api/notificationApi";
const Header = () => {
    const navigate = useNavigate();

    const loggedIn = isLoggedIn();
    const user = getCurrentUser();
    const initial = user?.nickname?.charAt(0).toUpperCase() ?? "U";

    const { unreadCount, setUnreadCount } = useSSE(loggedIn);
    const [isNotiOpen, setIsNotiOpen] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const dropdownRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsNotiOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleNotiClick = async () => {
        const nextState = !isNotiOpen;
        setIsNotiOpen(nextState);

        // 드롭다운이 열릴 때 백엔드에서 알림 목록을 가져옵니다!
        if (nextState) {
            try {
                const res = await fetchMyNotifications();
                // ApiResponse.success() 구조라면 res.data 배열을 저장
                setNotifications(res.data || []);
            } catch (error) {
                console.error("알림 목록을 불러오지 못했습니다.", error);
            }
        }
    };

    const handleReadNotification = async (notiId) => {
        try {
            await markNotificationAsRead(notiId);
            // 클릭한 알림의 isRead 상태를 화면에서도 true로 변경
            setNotifications(prev => prev.map(n => n.id === notiId ? { ...n, isRead: true } : n));
            // 총 안 읽은 갯수 1 감소 (0 이하로 내려가지 않게)
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (error) {
            console.error(error);
        }
    };

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

                        <div className="noti-dropdown-container" ref={dropdownRef}>
                            <button
                                type="button"
                                className="app-header-noti-button"
                                onClick={handleNotiClick}
                            >
                                🔔
                                {unreadCount > 0 && <span className="noti-badge">{unreadCount}</span>}
                            </button>

                            {isNotiOpen && (
                                <div className="noti-dropdown-menu">
                                    <div className="noti-dropdown-header">
                                        <h4>알림</h4>
                                        <button className="noti-read-all-btn" onClick={markAllNotificationsAsRead}>
                                            모두 읽음
                                        </button>
                                    </div>
                                    <ul className="noti-list">
                                        {notifications.length === 0 ? (
                                            <li className="noti-empty">새로운 알림이 없습니다.</li>
                                        ) : (
                                            notifications.map((noti) => (
                                                <li
                                                    key={noti.id}
                                                    className={`noti-item ${noti.isRead ? 'read' : 'unread'}`}
                                                    onClick={() => handleReadNotification(noti.id)}
                                                >
                                                    <div className="noti-content">{noti.message}</div>
                                                    <div className="noti-time">방금 전</div> {/* 시간 포맷팅은 추후 적용 */}
                                                </li>
                                            ))
                                        )}
                                    </ul>
                                </div>
                            )}
                        </div>

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
