import { createPortal } from "react-dom";

/**
 * 토스트 알림 컴포넌트 — React Portal로 document.body에 렌더링
 * 부모의 CSS transform/overflow/stacking context에 영향받지 않음
 */
const TYPE_STYLES = {
    success: { background: "#10b981", color: "#ffffff" },   // 초록 - 즐겨찾기 추가
    error:   { background: "#ef4444", color: "#ffffff" },   // 빨강 - 오류
    warning: { background: "#f59e0b", color: "#ffffff" },   // 주황 - 경고
    info:    { background: "#3b82f6", color: "#ffffff" },   // 파랑 - 안내
    default: { background: "#1f2937", color: "#ffffff" },   // 진회색 - 즐겨찾기 해제
};

const Toast = ({ toast }) => {
    if (!toast) return null;

    const styles = TYPE_STYLES[toast.type] || TYPE_STYLES.default;

    return createPortal(
        <div
            style={{
                position: "fixed",
                bottom: "28px",
                right: "28px",
                zIndex: 99999,
                padding: "12px 20px",
                borderRadius: "12px",
                fontSize: "14px",
                fontWeight: 700,
                boxShadow: "0 4px 20px rgba(0,0,0,0.18)",
                maxWidth: "320px",
                background: styles.background,
                color: styles.color,
                pointerEvents: "none",
                userSelect: "none",
            }}
        >
            {toast.message}
        </div>,
        document.body
    );
};

export default Toast;
