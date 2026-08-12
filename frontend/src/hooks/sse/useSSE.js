// src/hooks/useSSE.js
import { useState, useEffect } from 'react';
import { EventSourcePolyfill } from 'event-source-polyfill';

export const useSSE = (isLoggedIn) => {
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        // 로그인하지 않은 상태면 연결하지 않음
        if (!isLoggedIn) return;

        const token = localStorage.getItem("accessToken");
        // ||를 쓰면 배포 환경의 VITE_API_BASE_URL=""(상대 경로)가 falsy로 취급돼
        // 항상 localhost:8080으로 빠지므로 반드시 ??(nullish coalescing)를 사용해야 한다.
        const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

        // EventSourcePolyfill을 사용하면 헤더에 JWT 토큰을 담을 수 있습니다!
        const eventSource = new EventSourcePolyfill(`${API_BASE_URL}/api/notifications/subscribe`, {
            headers: {
                Authorization: `Bearer ${token}`
            },
            heartbeatTimeout: 3600000, // 1시간 (백엔드 설정과 동일하게)
        });

        // 1. 연결 성공 이벤트
        eventSource.onopen = () => {
            console.log("🟢 SSE 실시간 알림 채널 연결 성공!");
        };

        // 2. 백엔드에서 보낸 'sse' 이벤트 수신!
        eventSource.addEventListener('sse', (event) => {
            const data = event.data;
            console.log("🔔 [알림 수신]:", data);

            // 처음에 보내는 더미 데이터("EventStream Created...")는 무시
            if (typeof data === 'string' && data.includes("EventStream Created")) {
                return;
            }

            // 실제 알림 데이터가 오면 안 읽은 알림 숫자 1 증가!
            setUnreadCount(prev => prev + 1);

            // 화면 우측 하단에 시스템 알림 띄우기 (옵션)
            // alert("새로운 알림이 도착했습니다!");
        });

        // 3. 에러 발생 시 처리
        eventSource.onerror = (error) => {
            console.error("🔴 SSE 연결 에러 발생:", error);
            eventSource.close(); // 에러 시 파이프 닫기
        };

        // 컴포넌트가 언마운트(로그아웃 등) 될 때 연결 깔끔하게 해제
        return () => {
            eventSource.close();
            console.log("⚪ SSE 실시간 알림 채널 연결 해제");
        };
    }, [isLoggedIn]); // 로그인 상태가 바뀔 때마다 재실행

    return { unreadCount, setUnreadCount };
};