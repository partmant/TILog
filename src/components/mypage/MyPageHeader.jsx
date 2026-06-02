import '../../styles/mypage/MyPageHeader.css';

const MyPageHeader = () => {
    return (
        <header className="mypage-header">
            <div className="mypage-brand">
                <div className="mypage-logo">T</div>

                <div>
                    <strong>TILog</strong>
                    <span>Growth Dashboard</span>
                </div>
            </div>

            <nav className="mypage-nav">
                <button type="button">메인 피드</button>
                <button type="button">TIL 목록</button>
                <button type="button">피드백</button>
                <button className="active" type="button">마이페이지</button>
            </nav>

            <div className="mypage-actions">
                <button className="mypage-write-button" type="button">
                    TIL 작성하기
                </button>

                <button className="mypage-profile-button" type="button" aria-label="프로필">
                    <span>U</span>
                </button>
            </div>
        </header>
    );
};

export default MyPageHeader;
