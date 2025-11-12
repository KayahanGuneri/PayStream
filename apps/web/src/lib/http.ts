import { AuthError, ValidationError, ApiError } from './errors';

// uuid
function uuidv4(): string {
  return globalThis.crypto?.randomUUID?.() ?? Math.random().toString(36).slice(2);
}

// /api tabanı (Vite proxy)
const API_BASE = '/api';

// '/v1/...' gibi verilen yolu '/api/v1/...' yap
function buildUrl(path: string): string {
  const p = path.startsWith('/') ? path : `/${path}`;
  return `${API_BASE}${p}`;
}

async function parseBody(res: Response): Promise<unknown> {
  const text = await res.text();
  if (!text) return undefined;
  try { return JSON.parse(text); } catch { return text; }
}

async function handle<T>(res: Response): Promise<T> {
  const data = await parseBody(res);
  if (res.ok) return data as T;

  const { status } = res;
  if (status === 401 || status === 403) {
    throw new AuthError('Unauthorized or forbidden', status, data);
  }
  if (status === 422) {
    throw new ValidationError('Validation failed', data);
  }

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
  async get<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {
      ...init,
      method: 'GET',
      headers: {
        ...(init?.headers ?? {}),
        'x-correlation-id': uuidv4()
      }
    });
    return handle<T>(res);
  },

  async post<T, B = unknown>(path: string, body: B, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {
      ...init,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-correlation-id': uuidv4(),
        'idempotency-key': uuidv4(),
        ...(init?.headers ?? {})
      },
      body: JSON.stringify(body)
    });
    return handle<T>(res);
  },

  async put<T, B = unknown>(path: string, body: B, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {
      ...init,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'x-correlation-id': uuidv4(),
        ...(init?.headers ?? {})
      },
      body: JSON.stringify(body)
    });
    return handle<T>(res);
  },

  async delete<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(buildUrl(path), {
      ...init,
      method: 'DELETE',
      headers: {
        ...(init?.headers ?? {}),
        'x-correlation-id': uuidv4()
      }
    });
    return handle<T>(res);
  }
};
