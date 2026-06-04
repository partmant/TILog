import { useState } from "react";
import { useNavigate } from "react-router-dom";

// 헤더 관련 로직 관리 Hook
export function useHeader() {
    const navigate = useNavigate();

    const [keyword, setKeyword] = useState("");

    // 로고 클릭 시 메인 페이지 이동
    const handleMoveHome = () => {
        navigate("/");
    };

    // 마이페이지 이동
    const handleMoveMyPage = () => {
        navigate("/mypage");
    };

    // 검색 실행
    const handleSearch = () => {
        if (!keyword.trim()) return;

        navigate(`/posts?keyword=${encodeURIComponent(keyword.trim())}`);
    };

    // Enter 키 검색
    const handleSearchKeyDown = (e) => {
        if (e.key === "Enter") {
            handleSearch();
        }
    };

    return {
        keyword,
        setKeyword,
        handleMoveHome,
        handleMoveMyPage,
        handleSearch,
        handleSearchKeyDown,
    };
}