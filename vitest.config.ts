// vitest.config.ts (ROOT)

import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./apps/web/src/test/setup.ts'],
    include: ['apps/web/src/**/*.{test,spec}.{ts,tsx}'],
    css: true,
  },
});
