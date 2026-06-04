import { getCurrentUser } from '../../utils/authUtils';
import '../../styles/mypage/MyPageHero.css';

const MyPageHero = () => {
    const user = getCurrentUser();

    const nickname = user?.nickname ?? 'user';
    const email = user?.email ?? '';
    const initial = nickname.charAt(0).toUpperCase();
    const joinDate = user?.createdAt ? user.createdAt.replaceAll('-', '.') : null;

    return (
        <section className="mypage-hero">
            <div className="mypage-hero-left">
                <div className="mypage-avatar">
                    <span>{initial}</span>
                </div>

                <div>
                    <div className="mypage-profile-title">
                        <h1>{nickname}</h1>
                        <span>꾸준한 작성자</span>
                    </div>

                    <p>기록으로 성장하는 TIL 작성자</p>

                    <div className="mypage-profile-meta">
                        {email && <span>{email}</span>}
                        {joinDate && <span>가입일 {joinDate}</span>}
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
