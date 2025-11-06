/* Türkçe Özet:
   Vite dev sunucusunda '/api' isteklerini Gateway'e proxy'ler (8084).
   Böylece frontend fetch('/api/...') → http://localhost:8084/... olur.
*/
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // everything under /api goes to gateway
      '/api': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        // if your gateway serves /api already, you can leave rewrite as is
        // rewrite: (path) => path, // no-op
      },
    },
  },
});
