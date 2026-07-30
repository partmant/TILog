import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { addBookmark, removeBookmark } from "../../api/bookmark";
import { isLoggedIn } from "../../utils/authUtils";

/**
 * TIL 게시글 즐겨찾기 토글 Hook
 *
 * @param {function} onBookmarkChange - (postId, isBookmarked) => void  목록 상태 갱신 콜백
 * @param {function} showToast        - (message, type) => void  토스트 알림 콜백 (선택)
 */
export function useBookmark(onBookmarkChange, showToast) {
    const navigate = useNavigate();
    const [loadingPostId, setLoadingPostId] = useState(null);

    const handleToggleBookmark = async (postId, currentIsBookmarked) => {
        if (!isLoggedIn()) {
            navigate("/login");
            return;
        }

        if (loadingPostId === postId) return; // 중복 클릭 방지

        setLoadingPostId(postId);
        try {
            if (currentIsBookmarked) {
                await removeBookmark(postId);
                onBookmarkChange?.(postId, false);
                showToast?.("즐겨찾기가 해제되었습니다.", "default");
            } else {
                await addBookmark(postId);
                onBookmarkChange?.(postId, true);
                showToast?.("즐겨찾기에 추가되었습니다.", "success");
            }
        } catch (error) {
            console.error("[BOOKMARK ERROR]", error);
            showToast?.("즐겨찾기 처리 중 오류가 발생했습니다. 다시 시도해주세요.", "error");
        } finally {
            setLoadingPostId(null);
        }
    };

    return { handleToggleBookmark, loadingPostId };
}
