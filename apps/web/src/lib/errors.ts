/* Türkçe Özet:
   Bu dosya, HTTP istemcisi (lib/http.ts) tarafından oluşturulan
   özel hata sınıflarını içerir. Sunucudan gelen yanıt koduna göre
   uygun hata türü fırlatılır: 401/403 → AuthError, 422 → ValidationError,
   diğerleri → ApiError.
*/

// Base class for all API-related errors
export class ApiError extends Error {
  status?: number;
  data?: unknown;

  constructor(message: string, status?: number, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

// 422 validation failure (contains field-level errors)
export class ValidationError extends ApiError {
  constructor(message: string, data?: unknown) {
    super(message, 422, data);
    this.name = 'ValidationError';
  }
}

// 401/403 authorization/authentication failures
export class AuthError extends ApiError {
  constructor(message = 'Unauthorized or Forbidden', status?: number, data?: unknown) {
    super(message, status ?? 401, data);
    this.name = 'AuthError';
  }
}
