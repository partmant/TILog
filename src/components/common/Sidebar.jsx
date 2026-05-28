function Sidebar() {

    return (
        <aside className="flex h-[720px] w-[220px] flex-col rounded-3xl bg-slate-950 p-6 text-white">
            <h2 className="text-2xl font-bold">TILog</h2>
            <p className="mt-1 text-xs text-gray-400 underline">
                Growth Dashboard
            </p>

            <nav className="mt-12 space-y-5 text-sm font-semibold">
                <div className="px-4 py-3">⌂ 메인 피드</div>
                <div className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-4 py-3">
                    📚 TIL 목록
                </div>
                <div className="px-4 py-3">✍️ 작성하기</div>
                <div className="px-4 py-3">👤 마이페이지</div>
                <div className="px-4 py-3">🏆 랭킹</div>
                <div className="px-4 py-3">💎 구독</div>
            </nav>

            <div className="mt-auto rounded-2xl bg-slate-800 p-4">
                <p className="font-bold">오늘도 1 TIL</p>
                <p className="mt-2 text-xs text-gray-400">
                    꾸준함이 성장입니다
                </p>
            </div>
        </aside>
    );
}

export default Sidebar;