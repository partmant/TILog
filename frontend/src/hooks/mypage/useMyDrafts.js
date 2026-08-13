import { useEffect, useState } from "react";
import { getMyDrafts, deletePost } from "../../api/post";

export function useMyDrafts() {
    const [drafts, setDrafts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchDrafts = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await getMyDrafts();
            setDrafts(data);
        } catch (err) {
            console.error("[MY DRAFTS ERROR]", err);
            setError("임시저장 목록을 불러오는 중 오류가 발생했습니다.");
            setDrafts([]);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchDrafts();
    }, []);

    const handleDeleteDraft = async (postId) => {
        try {
            await deletePost(postId);
            setDrafts((prev) => prev.filter((draft) => draft.postId !== postId));
        } catch (err) {
            console.error("[DELETE DRAFT ERROR]", err);
            alert("임시저장 삭제에 실패했습니다.");
        }
    };

    return { drafts, isLoading, error, handleDeleteDraft, refetch: fetchDrafts };
}
