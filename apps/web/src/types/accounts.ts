// Türkçe Özet: Accounts modülü için kesin DTO tipleri. Create body formdan hem customerId hem currency alır.
// Balance yanıtı MINOR (tam sayı) birimlerinde gelir; raw (snake_case) → UI-normalized (camelCase) dönüşümü hook içinde yapılır.

export type CreateAccountFormValues = {
  customerId: string;
  currency: string; // e.g., "TRY" (3-letter uppercase)
};

// ⬇️ FIX: CreateAccountBody, mutation çağrısında formdan gelen her iki alanı da içerir.
// Hook, path'te customerId'yi kullanır; request body'de sadece { currency } gönderir.
export type CreateAccountBody = {
  customerId: string;
  currency: string;
};

// Backend AccountResponse used by both read and create
export type AccountDTO = {
  id: string;
  currency: string;                  // ISO 4217, e.g., TRY
  status: string;                    // ACTIVE | BLOCKED | CLOSED
  // NOTE: Add createdAt etc. when backend exposes them.
};

export type CreateAccountResponse = AccountDTO;

// -------- Balance DTOs (raw vs normalized) --------

// Raw response shape from API (snake_case, MINOR units).
export type AccountBalanceRawDTO = {
  account_id: string;
  balance_minor: number;             // integer, MINOR units
  as_of_ledger_offset: number | null;
  updated_at: string | null;
};

// UI-normalized shape (camelCase), still MINOR units authoritative.
export type AccountBalanceDTO = {
  accountId: string;
  balanceMinor: number;              // integer, MINOR units
  asOfLedgerOffset?: number | null;
  updatedAt?: string | null;
};
