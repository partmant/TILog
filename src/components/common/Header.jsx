import { NavLink, useNavigate } from "react-router-dom";
import { getCurrentUser, isLoggedIn, logout } from "../../utils/authUtils";
import { useHeader } from "../../hooks/common/useHeader";
import "../../styles/common/Header.css";

const NOTIFICATION_LABEL = {
    COMMENT: "댓글",
    LIKE: "좋아요",
    FOLLOW: "팔로우",
};

// 휴지통 아이콘
const TrashIcon = () => (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="3 6 5 6 21 6"/>
        <path d="M19 6l-1 14H6L5 6"/>
        <path d="M10 11v6M14 11v6"/>
        <path d="M9 6V4h6v2"/>
    </svg>
);

// 벨 아이콘
const BellIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
    </svg>
);

// 사람들 아이콘
const PeopleIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
);

const Header = () => {
    const navigate = useNavigate();
    const loggedIn = isLoggedIn();
    const user = getCurrentUser();
    const initial = user?.nickname?.charAt(0).toUpperCase() ?? "U";

    const handleLogout = () => {
        logout();
        navigate("/login", { replace: true });
    };

    const {
        showNotifications, notifications, unreadCount, notificationRef,
        handleToggleNotifications, handleMarkAsRead, handleMarkAllAsRead,
        handleDeleteNotification, handleDeleteAllNotifications,
        showFollowPanel, followTab, setFollowTab,
        followers, followings, followLoading,
        followPanelRef, handleToggleFollowPanel,
        handleNavigateToMemberTil,
    } = useHeader();

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
                <NavLink to="/feed" className={({ isActive }) =>
                    isActive ? "app-header-nav-link active" : "app-header-nav-link"}>
                    메인 피드
                </NavLink>
                <NavLink to="/posts" className={({ isActive }) =>
                    isActive ? "app-header-nav-link active" : "app-header-nav-link"}>
                    TIL 목록
                </NavLink>
                <NavLink to="/feedback" className={({ isActive }) =>
                    isActive ? "app-header-nav-link active" : "app-header-nav-link"}>
                    피드백
                </NavLink>
                {loggedIn && (
                    <NavLink to="/mypage" className={({ isActive }) =>
                        isActive ? "app-header-nav-link active" : "app-header-nav-link"}>
                        마이페이지
                    </NavLink>
                )}
            </nav>

            <div className="app-header-actions">
                {loggedIn ? (
                    <>
                        <button type="button" className="app-header-write-button"
                                onClick={() => navigate("/posts/write")}>
                            TIL 작성하기
                        </button>

                        {/* ── 팔로워/팔로잉 패널 ── */}
                        <div className="header-panel-wrapper" ref={followPanelRef}>
                            <button
                                type="button"
                                className={`app-header-icon-button ${showFollowPanel ? "active" : ""}`}
                                onClick={handleToggleFollowPanel}
                                aria-label="팔로워/팔로잉"
                            >
                                <PeopleIcon />
                            </button>

                            {showFollowPanel && (
                                <div className="header-dropdown-panel">
                                    {/* 패널 헤더 */}
                                    <div className="header-panel-head">
                                        <div className="header-panel-tabs">
                                            <button
                                                type="button"
                                                className={`header-panel-tab ${followTab === "followers" ? "active" : ""}`}
                                                onClick={() => setFollowTab("followers")}
                                            >
                                                팔로워
                                                {followers.length > 0 && (
                                                    <span className="header-panel-tab-count">
                                                        {followers.length}
                                                    </span>
                                                )}
                                            </button>
                                            <button
                                                type="button"
                                                className={`header-panel-tab ${followTab === "followings" ? "active" : ""}`}
                                                onClick={() => setFollowTab("followings")}
                                            >
                                                팔로잉
                                                {followings.length > 0 && (
                                                    <span className="header-panel-tab-count">
                                                        {followings.length}
                                                    </span>
                                                )}
                                            </button>
                                        </div>
                                    </div>

                                    {/* 목록 */}
                                    <ul className="header-panel-list">
                                        {followLoading ? (
                                            <li className="header-panel-empty">불러오는 중...</li>
                                        ) : (() => {
                                            const list = followTab === "followers" ? followers : followings;
                                            return list.length === 0 ? (
                                                <li className="header-panel-empty">
                                                    {followTab === "followers" ? "팔로워가 없습니다" : "팔로잉이 없습니다"}
                                                </li>
                                            ) : list.map((member) => (
                                                <li key={member.memberId} className="header-panel-follow-item">
                                                    <div className="header-panel-avatar">
                                                        {member.nickname?.charAt(0).toUpperCase()}
                                                    </div>
                                                    <span className="header-panel-nickname">
                                                        {member.nickname}
                                                    </span>
                                                    {followTab === "followings" && (
                                                        <button
                                                            type="button"
                                                            className="header-panel-til-btn"
                                                            onClick={() => handleNavigateToMemberTil(member.memberId)}
                                                        >
                                                            TIL 보기
                                                        </button>
                                                    )}
                                                </li>
                                            ));
                                        })()}
                                    </ul>
                                </div>
                            )}
                        </div>

                        {/* ── 알림 패널 ── */}
                        <div className="header-panel-wrapper" ref={notificationRef}>
                            <button
                                type="button"
                                className={`app-header-icon-button ${showNotifications ? "active" : ""}`}
                                onClick={handleToggleNotifications}
                                aria-label="알림"
                            >
                                <BellIcon />
                                {unreadCount > 0 && (
                                    <span className="app-header-notification-badge">
                                        {unreadCount > 99 ? "99+" : unreadCount}
                                    </span>
                                )}
                            </button>

                            {showNotifications && (
                                <div className="header-dropdown-panel">
                                    {/* 패널 헤더 */}
                                    <div className="header-panel-head">
                                        <span className="header-panel-title">알림</span>
                                        <div className="header-panel-actions">
                                            {notifications.some((n) => !n.isRead) && (
                                                <button type="button"
                                                        className="header-panel-action-btn"
                                                        onClick={handleMarkAllAsRead}>
                                                    모두 읽음
                                                </button>
                                            )}
                                            {notifications.length > 0 && (
                                                <button type="button"
                                                        className="header-panel-action-btn danger"
                                                        onClick={handleDeleteAllNotifications}>
                                                    모두 삭제
                                                </button>
                                            )}
                                        </div>
                                    </div>

                                    {/* 알림 목록 */}
                                    <ul className="header-panel-list">
                                        {notifications.length === 0 ? (
                                            <li className="header-panel-empty">새로운 알림이 없습니다</li>
                                        ) : notifications.map((n) => (
                                            <li
                                                key={n.notificationId}
                                                className={`header-panel-noti-item ${!n.isRead ? "unread" : ""}`}
                                                onClick={() => !n.isRead && handleMarkAsRead(n.notificationId)}
                                            >
                                                <span className="header-panel-noti-type">
                                                    {NOTIFICATION_LABEL[n.notificationType] ?? n.notificationType}
                                                </span>
                                                <span className="header-panel-noti-message">
                                                    {n.message}
                                                </span>
                                                <div className="header-panel-noti-right">
                                                    {!n.isRead && <span className="header-panel-noti-dot" />}
                                                    <button
                                                        type="button"
                                                        className="header-panel-noti-delete"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleDeleteNotification(n.notificationId);
                                                        }}
                                                        aria-label="알림 삭제"
                                                    >
                                                        <TrashIcon />
                                                    </button>
                                                </div>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>

                        <button type="button" className="app-header-profile-button"
                                onClick={() => navigate("/mypage")} aria-label="마이페이지로 이동">
                            {initial}
                        </button>

                        <button type="button" className="app-header-logout-button"
                                onClick={handleLogout} aria-label="로그아웃">
                            로그아웃
                        </button>
                    </>
                ) : (
                    <>
                        <button type="button" className="app-header-logout-button"
                                onClick={() => navigate("/login")}>
                            로그인
                        </button>
                        <button type="button" className="app-header-write-button"
                                onClick={() => navigate("/signup")}>
                            회원가입
                        </button>
                    </>
                )}
            </div>
        </header>
    );
};

export default Header;