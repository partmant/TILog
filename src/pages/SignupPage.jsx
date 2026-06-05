import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { request } from "../api/apiClient";
import "../styles/auth/Auth.css";

const CURRENT_STATUS_OPTIONS = [
    { value: "JOB_SEEKER", label: "취준생" },
    { value: "STUDENT", label: "학생" },
    { value: "EMPLOYED", label: "재직자" },
    { value: "CAREER_CHANGE", label: "이직준비자" },
    { value: "FREELANCER", label: "프리랜서" },
];

const TARGET_JOB_OPTIONS = [
    { value: "BACKEND", label: "백엔드 개발자" },
    { value: "FRONTEND", label: "프론트엔드 개발자" },
    { value: "FULLSTACK", label: "풀스택 개발자" },
    { value: "MOBILE_ANDROID", label: "안드로이드 앱 개발자" },
    { value: "MOBILE_IOS", label: "iOS 앱 개발자" },
    { value: "DATA_ENGINEER", label: "데이터 엔지니어" },
    { value: "AI_ML_ENGINEER", label: "AI / ML 엔지니어" },
    { value: "INFRA_DEVOPS", label: "인프라 / DevOps 엔지니어" },
    { value: "GAME_DEVELOPER", label: "게임 개발자" },
    { value: "EMBEDDED", label: "임베디드 / IoT 개발자" },
    { value: "PRODUCT_MANAGER", label: "기획자 / PM / PO" },
    { value: "ETC", label: "기타 / 미정" },
];

export default function SignupPage() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        email: "",
        nickname: "",
        password: "",
        passwordConfirm: "",
        currentStatus: "",
        targetJob: "",
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (form.password !== form.passwordConfirm) {
            setError("비밀번호가 일치하지 않습니다.");
            return;
        }
        setError("");
        setLoading(true);
        try {
            await request("/api/auth/signup", {
                method: "POST",
                body: JSON.stringify({
                    email: form.email,
                    nickname: form.nickname,
                    password: form.password,
                    currentStatus: form.currentStatus || null,
                    targetJob: form.targetJob || null,
                }),
            });
            navigate("/login");
        } catch (err) {
            setError(err.message || "회원가입에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card" style={{ maxWidth: 980 }}>
                {/* 좌측 폼 */}
                <div className="auth-form-panel">
                    <h2 className="auth-form-title">회원가입</h2>
                    <p className="auth-form-subtitle">TIL 작성 습관을 만들고 성장 데이터를 기록해보세요.</p>

                    <form className="auth-form" onSubmit={handleSubmit}>
                        {/* 이메일 + 닉네임 */}
                        <div className="auth-row">
                            <div className="auth-field">
                                <label htmlFor="email">이메일</label>
                                <input
                                    id="email"
                                    className="auth-input"
                                    type="email"
                                    name="email"
                                    placeholder="user01@email.com"
                                    value={form.email}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="auth-field">
                                <label htmlFor="nickname">닉네임</label>
                                <input
                                    id="nickname"
                                    className="auth-input"
                                    type="text"
                                    name="nickname"
                                    placeholder="user01"
                                    value={form.nickname}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        {/* 비밀번호 */}
                        <div className="auth-row">
                            <div className="auth-field">
                                <label htmlFor="password">비밀번호</label>
                                <input
                                    id="password"
                                    className="auth-input"
                                    type="password"
                                    name="password"
                                    placeholder="영문+숫자 8자 이상"
                                    value={form.password}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="auth-field">
                                <label htmlFor="passwordConfirm">비밀번호 확인</label>
                                <input
                                    id="passwordConfirm"
                                    className="auth-input"
                                    type="password"
                                    name="passwordConfirm"
                                    placeholder="다시 입력"
                                    value={form.passwordConfirm}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        {/* 현재 상태 + 목표 직군 */}
                        <div className="auth-row">
                            <div className="auth-field">
                                <label htmlFor="currentStatus">현재 상태 <span style={{ color: "#9ca3af", fontWeight: 400 }}>(선택)</span></label>
                                <select
                                    id="currentStatus"
                                    className="auth-input"
                                    name="currentStatus"
                                    value={form.currentStatus}
                                    onChange={handleChange}
                                >
                                    <option value="">선택 안 함</option>
                                    {CURRENT_STATUS_OPTIONS.map((opt) => (
                                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="auth-field">
                                <label htmlFor="targetJob">목표 직군 <span style={{ color: "#9ca3af", fontWeight: 400 }}>(선택)</span></label>
                                <select
                                    id="targetJob"
                                    className="auth-input"
                                    name="targetJob"
                                    value={form.targetJob}
                                    onChange={handleChange}
                                >
                                    <option value="">선택 안 함</option>
                                    {TARGET_JOB_OPTIONS.map((opt) => (
                                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="auth-checkbox-group">
                            <label className="auth-checkbox-label">
                                <input type="checkbox" required />
                                이용약관과 개인정보 처리방침에 동의합니다.
                            </label>
                            <label className="auth-checkbox-label">
                                <input type="checkbox" />
                                TIL 작성 리마인드 알림을 받겠습니다.
                            </label>
                        </div>

                        {error && <p className="auth-error">{error}</p>}

                        <button type="submit" className="auth-submit-btn" disabled={loading}>
                            {loading ? "처리 중..." : "회원가입"}
                        </button>
                    </form>

                    <p className="auth-switch-text" style={{ marginTop: 20 }}>
                        이미 계정이 있나요?{" "}
                        <button type="button" className="auth-text-btn" onClick={() => navigate("/login")}>
                            로그인
                        </button>
                    </p>
                </div>

                {/* 우측 브랜딩 */}
                <div className="auth-brand-panel auth-brand-panel--right">
                    <div className="auth-brand-blob1" />
                    <div className="auth-brand-blob3" />
                    <div className="auth-brand-content">
                        <h2 className="auth-brand-title">기록은 성장의<br />시작입니다.</h2>
                        <p className="auth-brand-subtitle">
                            매일 작성한 TIL이 스트릭과 잔디로 쌓이고,<br />
                            나만의 성장 리포트가 됩니다.
                        </p>
                        <div className="auth-brand-divider" />
                        <p className="auth-brand-feature-heading">가입 후 사용할 수 있는 기능</p>
                        <ul className="auth-brand-features">
                            <li><span>✓</span> TIL 게시글 작성 및 관리</li>
                            <li><span>✓</span> 작성 이력 자동 기록</li>
                            <li><span>✓</span> 잔디 히트맵과 스트릭 통계</li>
                            <li><span>✓</span> 페이백 챌린지 참여 준비</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
}
