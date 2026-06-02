import { BrowserRouter, Route, Routes } from 'react-router-dom';
import MyPage from './pages/MyPage';

const App = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/mypage" element={<MyPage />} />
            </Routes>
        </BrowserRouter>
    );
};

export default App;
