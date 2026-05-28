"use strict";

import axios from "axios";

// 공통 axios 인스턴스 생성
const api = axios.create({
    baseURL: "http://localhost:8080",
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