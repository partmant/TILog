import { useState, useCallback, useRef } from "react";

/**
 * 토스트 알림 Hook
 * showToast(message, type) 호출 시 2.5초 후 자동 소멸
 */
export function useToast() {
    const [toast, setToast] = useState(null);
    const timerRef = useRef(null);

    const showToast = useCallback((message, type = "default") => {
        if (timerRef.current) clearTimeout(timerRef.current);
        setToast({ message, type });
        timerRef.current = setTimeout(() => {
            setToast(null);
        }, 2500);
    }, []);

    return { toast, showToast };
}
