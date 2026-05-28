function Header() {
    return (
        <header className="flex items-center justify-between rounded-3xl bg-white/90 px-6 py-4 shadow-sm">
            {/* 로고 */}
            <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-purple-500 to-cyan-400 text-xl font-bold text-white">
                    T
                </div>
                <h1 className="text-2xl font-bold">
                    TILog
                </h1>
            </div>

            {/* 검색창 */}
            <div className="flex w-[420px] gap-3">
                <input
                    className="flex-1 rounded-xl border border-gray-200 px-4 py-2 outline-none"
                    placeholder="검색어, 기술 스택, 작성자 검색"
                />
                <button className="rounded-xl bg-gradient-to-r from-purple-500 to-cyan-400 px-7 py-2 font-bold text-white">
                    검색
                </button>
            </div>

            {/* 유저 버튼 */}
            <button className="rounded-xl border px-8 py-2 font-bold">
                유저123
            </button>
        </header>
    );
}

export default Header;