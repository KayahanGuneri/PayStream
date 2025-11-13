// Türkçe Özet:
// Test ortamını hazırlar: MSW sunucusu başlatma/durdurma, jest-dom matcher'larını yükler.

import '@testing-library/jest-dom/vitest';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { server } from './server/handlers';

// Start/stop MSW
beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
