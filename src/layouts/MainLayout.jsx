import { Outlet } from "react-router-dom";

import Header from "../components/common/Header.jsx";
import Sidebar from "../components/common/Sidebar.jsx";

// 공통 레이아웃

function MainLayout() {
    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-100 p-6">
            <Header />
            <div className="mt-7 flex gap-7">
                <Sidebar />
                <main className="flex-1">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default MainLayout;