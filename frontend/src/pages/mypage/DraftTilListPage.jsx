import { useNavigate } from "react-router-dom";
import { useMyDrafts } from "../../hooks/mypage/useMyDrafts";
import { formatDateText } from "../../utils/mypageUtils";

const DIFFICULTY_LABEL = { EASY: "쉬움", NORMAL: "보통", HARD: "어려움" };

const DIFFICULTY_STYLE = {
    EASY: "bg-[#E8F7E7] text-[#62C15B]",
    NORMAL: "bg-indigo-50 text-indigo-500",
    HARD: "bg-[#FDECEC] text-[#E44343]",
};

function DraftTilListPage() {
    const navigate = useNavigate();
    const { drafts, isLoading, error, handleDeleteDraft } = useMyDrafts();

    return (
        <main className="space-y-7">
            {/* 상단 헤더 배너 */}
            <section className="rounded-3xl border-2 border-purple-300 bg-gradient-to-r from-purple-50 to-indigo-50 p-8">
                <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                        <h2 className="text-3xl font-bold">임시저장함</h2>
                        <p className="mt-2 text-gray-600">
                            이어서 작성할 수 있는 임시저장 TIL 목록입니다.
                        </p>
                    </div>
                    <button
                        type="button"
                        onClick={() => navigate("/mypage")}
                        className="rounded-xl border border-gray-200 bg-white px-5 py-2 text-sm font-bold text-gray-500 transition hover:bg-gray-50"
                    >
                        ← 돌아가기
                    </button>
                </div>
            </section>

            {/* 임시저장 목록 */}
            <section className="rounded-3xl bg-white p-8 shadow-sm">
                <h3 className="mb-6 text-xl font-bold">
                    임시저장 {drafts.length}개
                </h3>

                {isLoading ? (
                    <div className="py-20 text-center text-gray-400 font-bold">
                        임시저장 목록을 불러오는 중입니다...
                    </div>
                ) : error ? (
                    <div className="py-20 text-center text-red-400 font-bold">{error}</div>
                ) : drafts.length === 0 ? (
                    <div className="py-20 text-center">
                        <p className="text-lg font-bold text-gray-400">
                            임시저장된 TIL이 없습니다.
                        </p>
                        <p className="mt-2 text-sm text-gray-300">
                            글쓰기에서 임시 저장을 누르면 여기서 이어서 작성할 수 있어요.
                        </p>
                        <button
                            type="button"
                            onClick={() => navigate("/posts/write")}
                            className="mt-6 rounded-full bg-purple-500 px-8 py-2 text-sm font-bold text-white hover:bg-purple-600"
                        >
                            TIL 작성하러 가기
                        </button>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {drafts.map((draft) => (
                            <div
                                key={draft.postId}
                                onClick={() => navigate(`/posts/${draft.postId}/edit`)}
                                className="cursor-pointer rounded-2xl border border-gray-100 border-l-4 border-l-purple-400 bg-white p-5 shadow-sm transition hover:shadow-md"
                            >
                                <div className="flex items-start justify-between gap-4">
                                    <div className="min-w-0">
                                        <h4 className="text-lg font-bold">
                                            {draft.title || "제목 없음"}
                                        </h4>
                                        <p className="mt-2 text-sm text-gray-500">
                                            {draft.studyTime ? `학습시간 ${draft.studyTime}분 · ` : ""}
                                            {formatDateText(draft.updatedAt)} 수정
                                        </p>
                                    </div>

                                    <div className="flex shrink-0 items-center gap-3">
                                        <span
                                            className={`rounded-full px-5 py-2 text-sm font-bold ${
                                                DIFFICULTY_STYLE[draft.difficulty] || DIFFICULTY_STYLE.NORMAL
                                            }`}
                                        >
                                            {DIFFICULTY_LABEL[draft.difficulty] ?? draft.difficulty}
                                        </span>

                                        <button
                                            type="button"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                if (window.confirm("이 임시저장 TIL을 삭제할까요?")) {
                                                    handleDeleteDraft(draft.postId);
                                                }
                                            }}
                                            className="rounded-full border border-gray-200 px-4 py-2 text-xs font-bold text-gray-500 transition hover:border-red-200 hover:bg-red-50 hover:text-red-500"
                                        >
                                            삭제
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </main>
    );
}

export default DraftTilListPage;
