import { useEffect, useRef, useState } from 'react';
import { getCurrentUser } from '../../utils/authUtils';
import { getMyProfile, updateProfileImage } from '../../api/memberApi';
import '../../styles/mypage/MyPageHero.css';
import EditProfileModal from './EditProfileModal';

const MyPageHero = () => {
    const user = getCurrentUser();
    const joinDate = user?.createdAt ? user.createdAt.replaceAll('-', '.') : null;

    const [profile, setProfile] = useState({
        nickname: user?.nickname ?? 'user',
        email: user?.email ?? '',
        currentStatus: null,
        targetJob: null,
    });
    const [profileImageUrl, setProfileImageUrl] = useState(null);
    const [isUploading, setIsUploading] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const fileInputRef = useRef(null);

    useEffect(() => {
        getMyProfile()
            .then((data) => {
                setProfileImageUrl(data?.profileImageUrl ?? null);
                setProfile((prev) => ({
                    ...prev,
                    nickname: data?.nickname ?? prev.nickname,
                    email: data?.email ?? prev.email,
                    currentStatus: data?.currentStatus ?? null,
                    targetJob: data?.targetJob ?? null,
                }));
            })
            .catch(() => {});
    }, []);

    const handleAvatarClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;
        if (!file.type.startsWith('image/')) {
            alert('이미지 파일만 업로드 가능합니다.');
            return;
        }
        try {
            setIsUploading(true);
            const data = await updateProfileImage(file);
            setProfileImageUrl(data?.profileImageUrl ?? null);
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

    const handleProfileSaved = (updated) => {
        setProfile((prev) => ({
            ...prev,
            nickname: updated?.nickname ?? prev.nickname,
            currentStatus: updated?.currentStatus !== undefined ? updated.currentStatus : prev.currentStatus,
            targetJob: updated?.targetJob !== undefined ? updated.targetJob : prev.targetJob,
        }));
    };

    return (
        <>
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
                            alt={`${profile.nickname} 프로필`}
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
                            <h1>{profile.nickname}</h1>
                            <span>꾸준한 작성자</span>
                        </div>

                        <p>기록으로 성장하는 TIL 작성자</p>

                        <div className="mypage-profile-meta">
                            {profile.email && <span>{profile.email}</span>}
                            {joinDate && <span>가입일 {joinDate}</span>}
                        </div>

                        <button
                            type="button"
                            className="mypage-edit-profile-btn"
                            onClick={() => setShowEditModal(true)}
                        >
                            ✏️ 개인정보 수정
                        </button>
                    </div>
                </div>

                <blockquote>
                    작은 기록이 쌓여
                    <br />
                    나만의 성장이 됩니다.
                </blockquote>
            </section>

            {showEditModal && (
                <EditProfileModal
                    initialData={{
                        nickname: profile.nickname,
                        currentStatus: profile.currentStatus,
                        targetJob: profile.targetJob,
                    }}
                    onClose={() => setShowEditModal(false)}
                    onSaved={handleProfileSaved}
                />
            )}
        </>
    );
};
export default MyPageHero;
