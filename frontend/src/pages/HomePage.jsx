import { useNavigate } from "react-router-dom";

function HomePage() {
    const navigate = useNavigate();

    // 게시글 목록 페이지 이동
    const handleMovePostPage = () => {
        navigate("/posts");
    };

    return (
        <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-br from-blue-50 via-white to-cyan-100">
            <h1 className="text-6xl font-extrabold text-slate-900">
                TILog
            </h1>
            <p className="mt-4 text-lg text-slate-600">
                개발자 TIL 커뮤니티
            </p>
            <button
                onClick={handleMovePostPage}
                className="mt-10 rounded-2xl bg-purple-500 px-10 py-4 text-lg font-bold text-white shadow-lg transition hover:bg-purple-600"
            >
                게시글 보러가기
            </button>
        </div>
    );
}

export default HomePage;