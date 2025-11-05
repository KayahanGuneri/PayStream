/* Türkçe Özet:
   HTTP istemcisi (fetch wrapper). 
   Tüm isteklerde x-correlation-id ekler. 
   POST/PUT/PATCH isteklerinde idempotency-key ekler.
   Hatalarda özel tipler (AuthError, ValidationError, ApiError) fırlatır.
*/

import { AuthError, ValidationError, ApiError } from './errors';

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
