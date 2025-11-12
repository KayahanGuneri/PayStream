import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Frontend -> Gateway proxy: /api → http://localhost:8084
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8084',
        changeOrigin: true
        // rewrite gerekmez; http.ts zaten '/api' ile başlatıyor.
      }
    }
  }
});
