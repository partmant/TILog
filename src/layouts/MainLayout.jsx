import { Outlet } from "react-router-dom";
import Header from "../components/common/Header";
import Footer from "../components/common/Footer";
import "../styles/mypage/Mypage.css";

const MainLayout = () => {
    return (
        <main className="mypage">
            <div className="mypage-shell">
                <Header />
                <Outlet />
                <Footer />
            </div>
        </main>
    );
};

export default MainLayout;
