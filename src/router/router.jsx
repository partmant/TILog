import { createBrowserRouter } from "react-router-dom";

// 레이아웃
import MainLayout from "../layouts/MainLayout.jsx";

// 메인 페이지
import HomePage from "../pages/HomePage.jsx";

// 로그인 페이지
import LoginPage from "../pages/LoginPage.jsx";
import SignupPage from "../pages/SignupPage.jsx";

// 마이 페이지
import MyPage from "../pages/MyPage.jsx";
import WeeklyReportDetailPage from "../pages/WeeklyReportDetailPage.jsx";

// 게시글 관련 페이지
import FeedPage from "../pages/feed/FeedPage.jsx";
import PostListPage from "../pages/post/PostListPage.jsx";
import PostDetailPage from "../pages/post/PostDetailPage.jsx";
import PostWritePage from "../pages/post/PostWritePage.jsx";

// 관리자 페이지
import AdminPage from "../pages/admin/AdminPage.jsx";

// 피드백 페이지
import FeedbackPage from "../pages/feedbacks/FeedbackPage.jsx";

// 인증 라우트
import PrivateRoute from "./PrivateRoute";

// 팔로잉한 피드 목록
import MemberTilListPage from "../pages/post/MemberTilListPage";

// 내 TIL 전체 보기
import MyTilListPage from "../pages/post/MyTilListPage";

// 즐겨찾기 전체 보기
import BookmarkedTilListPage from "../pages/mypage/BookmarkedTilListPage";

const router = createBrowserRouter([
    // =========================
    // 공개 페이지
    // =========================

    // 메인 페이지 경로
    {path: "/", element: <HomePage />,},
    // 로그인 페이지 경로
    {path: "/login", element: <LoginPage />,},
    // 회원가입 페이지 경로
    {path: "/signup", element: <SignupPage />,},

    // =========================
    // 공통 레이아웃 적용 페이지
    // =========================
    {element: <MainLayout />,
        children: [
            // 피드 경로
            { path: "/feed", element: <FeedPage /> },

            // 게시글 경로
            { path: "/posts", element: <PostListPage/>,},
            // 게시글 상세 페이지 경로
            { path: "/posts/:postId", element: <PostDetailPage/>,},
            // 팔로잉한 피드 목록
            { path: "/members/:memberId/tils", element: <MemberTilListPage /> },
        ],
    },

    // =========================
    // 로그인 사용자 전용 페이지
    // =========================
    {
        element: (
            <PrivateRoute>
                <MainLayout />
            </PrivateRoute>
        ),
        children: [
        // 마이 페이지 경로
        { path: "/mypage", element: <MyPage /> },
        // 내 TIL 전체 보기 경로
        { path: "/mypage/tils", element: <MyTilListPage /> },
        // 즐겨찾기 전체 보기 경로
        { path: "/mypage/bookmarks", element: <BookmarkedTilListPage /> },
        // 주간 리포트 상세 페이지 경로
        { path: "/weekly-report", element: <WeeklyReportDetailPage /> },
        // 게시글 작성 페이지 경로
        { path: "/posts/write", element: <PostWritePage />,},
        // 게시글 수정 페이지 경로
        { path: "/posts/:postId/edit", element: <PostWritePage />},
            // 관리자 페이지 경로
            { path: "/admin", element: <AdminPage /> },
            // 피드백 페이지 경로
            { path: "/feedback", element: <FeedbackPage /> },
        ],
    },
]);

export default router;