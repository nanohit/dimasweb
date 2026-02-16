import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
    plugins: [vue()],
    server: {
        port: 5173,
        proxy: {
            // Для разработки можно настроить прокси, чтобы избежать CORS
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
});