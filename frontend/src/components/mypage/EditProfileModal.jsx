import { useState } from 'react';
import { updateProfile } from '../../api/memberApi';
import '../../styles/mypage/EditProfileModal.css';

const CURRENT_STATUS_OPTIONS = [
    { value: '', label: '선택 안함' },
    { value: '취준생', label: '취준생' },
    { value: '학생', label: '학생' },
    { value: '재직자', label: '재직자' },
    { value: '이직준비자', label: '이직준비자' },
    { value: '프리랜서', label: '프리랜서' },
];

const TARGET_JOB_OPTIONS = [
    { value: '', label: '선택 안함' },
    { value: '백엔드 개발자', label: '백엔드 개발자' },
    { value: '프론트엔드 개발자', label: '프론트엔드 개발자' },
    { value: '풀스택 개발자', label: '풀스택 개발자' },
    { value: '안드로이드 앱 개발자', label: '안드로이드 앱 개발자' },
    { value: 'iOS 앱 개발자', label: 'iOS 앱 개발자' },
    { value: '데이터 엔지니어', label: '데이터 엔지니어' },
    { value: 'AI / ML 엔지니어', label: 'AI / ML 엔지니어' },
    { value: '인프라 / DevOps 엔지니어', label: '인프라 / DevOps 엔지니어' },
    { value: '게임 개발자', label: '게임 개발자' },
    { value: '임베디드 / IoT 개발자', label: '임베디드 / IoT 개발자' },
    { value: '기획자 / PM / PO', label: '기획자 / PM / PO' },
    { value: '기타 / 미정', label: '기타 / 미정' },
];

const EditProfileModal = ({ initialData, onClose, onSaved }) => {
    const [nickname, setNickname] = useState(initialData?.nickname ?? '');
    const [currentStatus, setCurrentStatus] = useState(initialData?.currentStatus ?? '');
    const [targetJob, setTargetJob] = useState(initialData?.targetJob ?? '');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async () => {
        if (nickname.trim().length < 2 || nickname.trim().length > 20) {
            setError('닉네임은 2자 이상 20자 이하여야 합니다.');
            return;
        }
        try {
            setLoading(true);
            setError('');
            const updated = await updateProfile({
                nickname: nickname.trim(),
                currentStatus: currentStatus || null,
                targetJob: targetJob || null,
            });
            onSaved(updated);
            onClose();
        } catch (e) {
            setError(e.message || '수정에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleBackdropClick = (e) => {
        if (e.target === e.currentTarget) onClose();
    };

    return (
        <div className="epm-backdrop" onClick={handleBackdropClick}>
            <div className="epm-modal" role="dialog" aria-modal="true" aria-label="개인정보 수정">
                <div className="epm-header">
                    <h2 className="epm-title">개인정보 수정</h2>
                    <button className="epm-close" onClick={onClose} aria-label="닫기">✕</button>
                </div>

                <div className="epm-body">
                    <label className="epm-label">
                        닉네임
                        <input
                            className="epm-input"
                            type="text"
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                            maxLength={20}
                            placeholder="닉네임 입력"
                        />
                    </label>

                    <label className="epm-label">
                        현재 상태
                        <select
                            className="epm-select"
                            value={currentStatus}
                            onChange={(e) => setCurrentStatus(e.target.value)}
                        >
                            {CURRENT_STATUS_OPTIONS.map((o) => (
                                <option key={o.value} value={o.value}>{o.label}</option>
                            ))}
                        </select>
                    </label>

                    <label className="epm-label">
                        목표 직군
                        <select
                            className="epm-select"
                            value={targetJob}
                            onChange={(e) => setTargetJob(e.target.value)}
                        >
                            {TARGET_JOB_OPTIONS.map((o) => (
                                <option key={o.value} value={o.value}>{o.label}</option>
                            ))}
                        </select>
                    </label>

                    {error && <p className="epm-error">{error}</p>}
                </div>

                <div className="epm-footer">
                    <button className="epm-btn-cancel" onClick={onClose} disabled={loading}>
                        취소
                    </button>
                    <button className="epm-btn-save" onClick={handleSubmit} disabled={loading}>
                        {loading ? '저장 중...' : '저장'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EditProfileModal;
