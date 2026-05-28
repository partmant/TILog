import { createBrowserRouter } from "react-router-dom";

// 레이아웃
import MainLayout from "../layouts/MainLayout.jsx";

// 메인 페이지
import HomePage from "../pages/HomePage.jsx";

// 게시글 관련 페이지
import PostListPage from "../pages/post/PostListPage.jsx";
import PostDetailPage from "../pages/post/PostDetailPage.jsx";
import PostWritePage from "../pages/post/PostWritePage.jsx";

const router = createBrowserRouter([
    // 메인 경로
    {path: "/", element: <HomePage />,},

    // 공통 레이아웃
    {element: <MainLayout />,
        children: [
            // 게시글 경로
            {path: "/posts", element: <PostListPage/>,},
            {path: "/posts/:postId", element: <PostDetailPage/>,},
            {path: "/posts/write", element: <PostWritePage />,},
            {path: "/posts/:postId/edit", element: <PostWritePage />},
        ],
    },
]);

export default router;