import { NavLink } from "react-router-dom";

function Sidebar() {
    const menuItemClass = ({ isActive }) =>
        [
            "block rounded-xl px-4 py-3 transition",
            isActive
                ? "bg-gradient-to-r from-purple-500 to-cyan-400"
                : "hover:bg-slate-800",
        ].join(" ");

    return (
        <aside className="flex h-[720px] w-[220px] flex-col rounded-3xl bg-slate-950 p-6 text-white">
            {/* 사이드바 로고 영역 */}
            <div>
                <h2 className="text-2xl font-bold">TILog</h2>

                <NavLink
                    to="/dashboard"
                    className="mt-1 block text-xs text-gray-400 underline"
                >
                    Growth Dashboard
                </NavLink>
            </div>

            {/* 사이드바 메뉴 */}
            <nav className="mt-12 space-y-5 text-sm font-semibold">
                <NavLink to="/" className={menuItemClass}>
                    ⌂ 메인 피드
                </NavLink>

                <NavLink to="/posts" end className={menuItemClass}>
                    📚 TIL 목록
                </NavLink>

                <NavLink to="/posts/write" className={menuItemClass}>
                    ✍️ 작성하기
                </NavLink>

                <NavLink to="/mypage" className={menuItemClass}>
                    👤 마이페이지
                </NavLink>

                <NavLink to="/ranking" className={menuItemClass}>
                    🏆 랭킹
                </NavLink>

                <NavLink to="/subscription" className={menuItemClass}>
                    💎 구독
                </NavLink>
            </nav>

            {/* 하단 문구 영역 */}
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