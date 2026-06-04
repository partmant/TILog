import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { request } from "../api/apiClient";
import { clearAuthStorage } from "../utils/authUtils";
import "../styles/auth/Auth.css";

export default function LoginPage() {
    const navigate = useNavigate();
    const [form, setForm] = useState({ email: "", password: "" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            const res = await request("/api/auth/login", {
                method: "POST",
                body: JSON.stringify({ email: form.email, password: form.password }),
            });
            // apiClient가 data.data / data.result 자동 언래핑
            const token = res?.accessToken ?? res;
            clearAuthStorage();
            localStorage.setItem("accessToken", token);
            navigate("/mypage");
        } catch (err) {
            setError(err.message || "로그인에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card">
                {/* 좌측 브랜딩 */}
                <div className="auth-brand-panel">
                    <div className="auth-brand-blob1" />
                    <div className="auth-brand-blob2" />
                    <div className="auth-brand-content">
                        <h1 className="auth-brand-title">TILog</h1>
                        <p className="auth-brand-subtitle">
                            하루의 학습을 기록하고,<br />
                            꾸준한 성장을<br />
                            시각화하세요.
                        </p>
                        <ul className="auth-brand-features">
                            <li><span>✓</span> TIL 작성 기록 저장</li>
                            <li><span>✓</span> 연속 작성 스트릭 확인</li>
                            <li><span>✓</span> 잔디 히트맵으로 성장 추적</li>
                        </ul>
                    </div>
                </div>

                {/* 우측 폼 */}
                <div className="auth-form-panel">
                    <h2 className="auth-form-title">로그인</h2>
                    <p className="auth-form-subtitle">오늘의 TIL을 기록하러 돌아오셨군요.</p>

                    <form className="auth-form" onSubmit={handleSubmit}>
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
                            />
                        </div>

                        <div className="auth-field">
                            <label htmlFor="password">비밀번호</label>
                            <input
                                id="password"
                                className="auth-input"
                                type="password"
                                name="password"
                                placeholder="••••••••"
                                value={form.password}
                                onChange={handleChange}
                            />
                        </div>

                        <div className="auth-options-row">
                            <label className="auth-checkbox-label">
                                <input type="checkbox" />
                                로그인 상태 유지
                            </label>
                            <button type="button" className="auth-forgot-btn">비밀번호 찾기</button>
                        </div>

                        {error && <p className="auth-error">{error}</p>}

                        <button type="submit" className="auth-submit-btn" disabled={loading}>
                            {loading ? "로그인 중..." : "로그인"}
                        </button>
                    </form>

                    <button type="button" className="auth-google-btn" style={{ marginTop: 10 }}>
                        Google로 계속하기
                    </button>

                    <p className="auth-switch-text" style={{ marginTop: 20 }}>
                        아직 계정이 없나요?{" "}
                        <button type="button" className="auth-text-btn" onClick={() => navigate("/signup")}>
                            회원가입
                        </button>
                    </p>
                </div>
            </div>
        </div>
    );
}
