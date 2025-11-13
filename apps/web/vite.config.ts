/* Türkçe Özet:

   Vite dev sunucusunda '/api' isteklerini Gateway'e proxy'ler (8084).
   Böylece frontend fetch('/api/...') → http://localhost:8084/... olur.
*/
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {

      '/api': {

        target: 'http://localhost:9000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),


      // everything under /api goes to gateway
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
});
