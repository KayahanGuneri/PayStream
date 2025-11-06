/* Türkçe Özet:
   HTTP istemcisi (fetch wrapper). Vite proxy ile Gateway'e '/api' tabanı üzerinden gider.
   Tüm isteklerde x-correlation-id ekler, yalnızca POST'ta idempotency-key ekler.
   401/403 → AuthError, 422 → ValidationError, diğer durumlar → ApiError fırlatır.

   HTTP istemcisi (fetch wrapper). 
   Tüm isteklerde x-correlation-id ekler. 
   POST/PUT/PATCH isteklerinde idempotency-key ekler.
   Hatalarda özel tipler (AuthError, ValidationError, ApiError) fırlatır.
*/

import { AuthError, ValidationError, ApiError } from './errors';

// Generate a correlation/idempotency id
function uuidv4(): string {
  // Use crypto if available; fallback provides uniqueness only for dev
  return (globalThis.crypto?.randomUUID?.() ?? Math.random().toString(36).slice(2));
}

// Base path that Vite proxy forwards to http://localhost:8084
const API_BASE = '/api';

// Ensure paths always start with a single slash then join with base
function buildUrl(path: string): string {
  const p = path.startsWith('/') ? path : `/${path}`;
  return `${API_BASE}${p}`;
}

// Parse response body (json or text)
async function parseBody(res: Response): Promise<unknown> {
  const text = await res.text();
  if (!text) return undefined;
  try { return JSON.parse(text); } catch { return text; }
}

// Map HTTP errors to our error classes
async function handle<T>(res: Response): Promise<T> {
  const data = await parseBody(res);
  if (res.ok) return data as T;

  const status = res.status;

  if (status === 401 || status === 403) {
    throw new AuthError('Unauthorized or forbidden', status, data);
  }
  if (status === 422) {
    // Backend validation error (body may carry per-field hints)
    throw new ValidationError('Validation failed', data);
  }

  // Try to pick a meaningful message from GlobalExceptionHandler body
  // Body shape example: { timestamp, status, error, message }
  let message = `HTTP ${status}`;
  if (typeof data === 'object' && data !== null && 'message' in data) {
    const m = (data as Record<string, unknown>).message;
    if (typeof m === 'string' && m.trim()) message = m;
  } else if (typeof data === 'string' && data.trim()) {
    message = data;
  }

  throw new ApiError(message, status, data);
}

export const http = {
  // GET /v1/...
  async get<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {
=======
// --- yardımcı uuid üretici ---
function uuidv4() {
  return crypto.randomUUID?.() ?? Math.random().toString(36).substring(2);
}

// --- ortak fetch handler ---
async function handle<T>(res: Response): Promise<T> {
  const text = await res.text();
  let data: unknown = undefined;
  try {
    data = text ? JSON.parse(text) : undefined;
  } catch {
    data = text;
  }

  if (res.ok) return data as T;

  // hata tipi sınıflandır
  const { status } = res;
  if (status === 401 || status === 403)
    throw new AuthError('Unauthorized or forbidden', status, data);
  if (status === 422)
    throw new ValidationError('Validation failed', data);
  function hasMessage(obj: unknown): obj is { message: string } {
    return typeof obj === 'object' && obj !== null && 'message' in obj && typeof (obj as Record<string, unknown>)['message'] === 'string';
  }
  const msg = hasMessage(data) ? data.message : `HTTP ${status}`;
  throw new ApiError(msg ?? 'Request failed', status, data);
}

// --- temel http nesnesi ---
export const http = {
  async get<T>(url: string, init?: RequestInit): Promise<T> {
    const res = await fetch(url, {
      ...init,
      method: 'GET',
      headers: {
        ...(init?.headers ?? {}),
        'x-correlation-id': uuidv4(),
      },
    });
    return handle<T>(res);
  },

  // POST /v1/...  (idempotency-key only here)
  async post<T, B = unknown>(path: string, body: B, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {

  async post<T, B = unknown>(url: string, body: B, init?: RequestInit): Promise<T> {
    const res = await fetch(url, {
      ...init,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-correlation-id': uuidv4(),
        'idempotency-key': uuidv4(),
        ...(init?.headers ?? {}),
      },
      body: JSON.stringify(body),
    });
    return handle<T>(res);
  },

  // PUT /v1/...  (no idempotency-key as requested)
  async put<T, B = unknown>(path: string, body: B, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {
  async put<T, B = unknown>(url: string, body: B, init?: RequestInit): Promise<T> {
    const res = await fetch(url, {
      ...init,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'x-correlation-id': uuidv4(),
        ...(init?.headers ?? {}),
      },
      body: JSON.stringify(body),
    });
    return handle<T>(res);
  },

  // DELETE /v1/...
  async delete<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {
  async delete<T>(url: string, init?: RequestInit): Promise<T> {
    const res = await fetch(url, {
      ...init,
      method: 'DELETE',
      headers: {
        ...(init?.headers ?? {}),
        'x-correlation-id': uuidv4(),
      },
    });
    return handle<T>(res);
  },
};
