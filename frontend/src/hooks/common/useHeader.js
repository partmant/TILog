import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
    getNotifications,
    getUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    deleteAllNotifications,
} from "../../api/notification";
import { getFollowers, getFollowings, unfollow } from "../../api/follow";

export function useHeader() {
    const navigate = useNavigate();
    const [keyword, setKeyword] = useState("");

    // ── 알림 ──
    const [showNotifications, setShowNotifications] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const notificationRef = useRef(null);

    // ── 팔로우 패널 ──
    const [showFollowPanel, setShowFollowPanel] = useState(false);
    const [followTab, setFollowTab] = useState("followers");
    const [followers, setFollowers] = useState([]);
    const [followings, setFollowings] = useState([]);
    const [followLoading, setFollowLoading] = useState(false);
    const followPanelRef = useRef(null);

    // 안 읽은 알림 수 폴링 (30초)
    useEffect(() => {
        const fetchUnread = async () => {
            try {
                const count = await getUnreadCount();
                setUnreadCount(count);
            } catch { /* 비로그인 무시 */ }
        };
        fetchUnread();
        const timer = setInterval(fetchUnread, 30000);
        return () => clearInterval(timer);
    }, []);

    // 알림 패널 외부 클릭 닫기
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (notificationRef.current && !notificationRef.current.contains(e.target)) {
                setShowNotifications(false);
            }
            if (followPanelRef.current && !followPanelRef.current.contains(e.target)) {
                setShowFollowPanel(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // 알림 패널 토글
    const handleToggleNotifications = async () => {
        if (!showNotifications) {
            try {
                const data = await getNotifications();
                setNotifications(data);
            } catch { setNotifications([]); }
        }
        setShowNotifications((prev) => !prev);
        setShowFollowPanel(false); // 다른 패널 닫기
    };

    // 단건 읽음
    const handleMarkAsRead = async (notificationId) => {
        try {
            await markAsRead(notificationId);
            setNotifications((prev) =>
                prev.map((n) => n.notificationId === notificationId ? { ...n, isRead: true } : n)
            );
            setUnreadCount((prev) => Math.max(0, prev - 1));
        } catch { /* ignore */ }
    };

    // 전체 읽음
    const handleMarkAllAsRead = async () => {
        try {
            await markAllAsRead();
            setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
            setUnreadCount(0);
        } catch { /* ignore */ }
    };

    // 단건 삭제
    const handleDeleteNotification = async (notificationId) => {
        try {
            await deleteNotification(notificationId);
            const deleted = notifications.find((n) => n.notificationId === notificationId);
            setNotifications((prev) => prev.filter((n) => n.notificationId !== notificationId));
            if (deleted && !deleted.isRead) setUnreadCount((prev) => Math.max(0, prev - 1));
        } catch { /* ignore */ }
    };

    // 전체 삭제
    const handleDeleteAllNotifications = async () => {
        try {
            await deleteAllNotifications();
            setNotifications([]);
            setUnreadCount(0);
        } catch { /* ignore */ }
    };

    // 팔로우 패널 토글
    const handleToggleFollowPanel = useCallback(async () => {
        if (!showFollowPanel) {
            setFollowLoading(true);
            setShowFollowPanel(true);
            setShowNotifications(false); // 다른 패널 닫기
            try {
                const [followerData, followingData] = await Promise.all([
                    getFollowers(),
                    getFollowings(),
                ]);
                setFollowers(followerData);
                setFollowings(followingData);
            } catch {
                setFollowers([]);
                setFollowings([]);
            } finally {
                setFollowLoading(false);
            }
        } else {
            setShowFollowPanel(false);
        }
    }, [showFollowPanel]);

    // 특정 회원 TIL 목록 페이지 이동
    const handleNavigateToMemberTil = (memberId) => {
        setShowFollowPanel(false);
        navigate(`/members/${memberId}/tils`);
    };

    // 팔로잉 패널에서 언팔로우
    const handleUnfollowFromPanel = async (memberId) => {
        try {
            await unfollow(memberId);
            setFollowings((prev) => prev.filter((m) => m.memberId !== memberId));
        } catch {
            alert("팔로우 취소 실패");
        }
    };

    const handleMoveHome = () => navigate("/");
    const handleMoveMyPage = () => navigate("/mypage");
    const handleSearch = () => {
        if (!keyword.trim()) return;
        navigate(`/posts?keyword=${encodeURIComponent(keyword.trim())}`);
    };
    const handleSearchKeyDown = (e) => {
        if (e.key === "Enter") handleSearch();
    };

    return {
        keyword, setKeyword,
        handleMoveHome, handleMoveMyPage, handleSearch, handleSearchKeyDown,
        // 알림
        showNotifications, notifications, unreadCount, notificationRef,
        handleToggleNotifications, handleMarkAsRead, handleMarkAllAsRead,
        handleDeleteNotification, handleDeleteAllNotifications,
        // 팔로우 패널
        showFollowPanel, followTab, setFollowTab,
        followers, followings, followLoading,
        followPanelRef, handleToggleFollowPanel,
        handleNavigateToMemberTil,
        handleUnfollowFromPanel,
    };
}