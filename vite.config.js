import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // 백엔드 Spring Boot 서버 (localhost:8080) 로 /api 요청 프록시
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})