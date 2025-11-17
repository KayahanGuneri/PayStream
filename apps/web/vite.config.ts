/* Türkçe Özet:
   Vite dev sunucusunu 5173 portunda çalıştırır ve '/api' isteklerini
   9000 portundaki Gateway'e proxy'ler.
   Testler için:
   - jsdom ortamı
   - setup.ts
   - React & ReactDOM alias'ları root node_modules'a yönlenir (tek React kopyası).
*/

import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

// ESM ortamında __dirname üretmek için
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      // Tüm 'react' ve 'react-dom' import'ları root node_modules'tan gelsin
      react: path.resolve(__dirname, '../../node_modules/react'),
      'react-dom': path.resolve(__dirname, '../../node_modules/react-dom'),
      'react-dom/client': path.resolve(
        __dirname,
        '../../node_modules/react-dom/client'
      ),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        rewrite: (pathStr) => pathStr.replace(/^\/api/, ''),
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    css: true,
  },
});
