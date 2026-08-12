import axios from "axios";

// 공통 axios 인스턴스 생성
// 배포 환경(VITE_API_BASE_URL="")에서는 baseURL을 빈 문자열(상대 경로)로 둬서
// nginx가 /api/를 백엔드로 프록시하도록 한다. ||를 쓰면 ""가 falsy라 항상
// localhost:8080으로 빠지므로 반드시 ??(nullish coalescing)를 사용해야 한다.
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
    withCredentials: true,
});

// 요청 인터셉터
api.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

// 응답 인터셉터
api.interceptors.response.use((response) => response, async (error) => {
        console.error(error);

        return Promise.reject(error);
    }
);

export default api;