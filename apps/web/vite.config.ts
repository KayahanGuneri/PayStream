/* Türkçe Özet:

   Vite dev sunucusunda '/api' isteklerini Gateway'e proxy'ler (8084).
   Böylece frontend fetch('/api/...') → http://localhost:8084/... olur.
*/

   Vite geliştirme sunucusu. /api path'ini yerel servislere proxy eder.
   Geçici çözüm: Gateway yerine doğrudan account-service (9000) hedeflenir.
*/


import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {

      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),


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
