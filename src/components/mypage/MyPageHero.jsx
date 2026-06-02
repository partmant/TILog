import '../../styles/mypage/MyPageHero.css';

const MyPageHero = () => {
    return (
        <section className="mypage-hero">
            <div className="mypage-hero-left">
                <div className="mypage-avatar">
                    <span>U</span>
                </div>

                <div>
                    <div className="mypage-profile-title">
                        <h1>user01</h1>
                        <span>꾸준한 작성자</span>
                    </div>

                    <p>기록으로 성장하는 TIL 작성자</p>

                    <div className="mypage-profile-meta">
                        <span>tilog@example.com</span>
                        <span>가입일 2026.05.01</span>
                    </div>
                </div>
            </div>

            <blockquote>
                작은 기록이 쌓여
                <br />
                나만의 성장이 됩니다.
            </blockquote>
        </section>
    );
};

export default MyPageHero;
