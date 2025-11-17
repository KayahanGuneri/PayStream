// Türkçe Özet:
// Test ortamını hazırlar: jest-dom matcher'larını yükler.
// Şu an global bir HTTP/MSW server'ı kullanmıyoruz; her test kendi spy/mock'unu kurar.

import '@testing-library/jest-dom/vitest';
