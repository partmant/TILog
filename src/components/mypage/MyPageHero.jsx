import { useEffect, useRef, useState } from 'react';
import { getCurrentUser } from '../../utils/authUtils';
import { getMyProfile, updateProfileImage } from '../../api/memberApi';
import '../../styles/mypage/MyPageHero.css';

const MyPageHero = () => {
    const user = getCurrentUser();
    const nickname = user?.nickname ?? 'user';
    const email = user?.email ?? '';
    const initial = nickname.charAt(0).toUpperCase();
    const joinDate = user?.createdAt ? user.createdAt.replaceAll('-', '.') : null;

    const [profileImageUrl, setProfileImageUrl] = useState(null);
    const [isUploading, setIsUploading] = useState(false);
    const fileInputRef = useRef(null);

    useEffect(() => {
        getMyProfile()
            .then((data) => setProfileImageUrl(data?.profileImageUrl ?? null))
            .catch(() => {});
    }, []);

    const handleAvatarClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        // 이미지 파일 타입 검증
        if (!file.type.startsWith('image/')) {
            alert('이미지 파일만 업로드 가능합니다.');
            return;
        }

        try {
            setIsUploading(true);
            const data = await updateProfileImage(file);
            setProfileImageUrl(data?.profileImageUrl ?? null);
            // 헤더에서도 반영되도록 custom event 발행
            window.dispatchEvent(new CustomEvent('profileImageUpdated', {
                detail: { profileImageUrl: data?.profileImageUrl }
            }));
        } catch {
            alert('프로필 이미지 업로드에 실패했습니다.');
        } finally {
            setIsUploading(false);
            e.target.value = '';
        }
    };

    return (
        <section className="mypage-hero">
            <div className="mypage-hero-left">
                <button
                    type="button"
                    className={`mypage-avatar mypage-avatar-btn ${isUploading ? 'uploading' : ''}`}
                    onClick={handleAvatarClick}
                    aria-label="프로필 이미지 변경"
                    title="클릭하여 프로필 이미지 변경"
                >
                    <img
                        src={profileImageUrl || '/default-profile.svg'}
                        alt={`${nickname} 프로필`}
                        className="mypage-avatar-img"
                    />
                    <div className="mypage-avatar-overlay">
                        {isUploading ? '업로드 중...' : '이미지 변경'}
                    </div>
                </button>

                <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    className="mypage-avatar-file-input"
                    onChange={handleFileChange}
                />

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
