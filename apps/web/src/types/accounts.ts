// Türkçe Özet:
// Accounts modülündeki tüm DTO tiplerinin tek doğruluk kaynağıdır.
// Backend sözleşmesine göre Account, CreateAccount ve Balance cevaplarını tanımlar.
// Balance endpoint snake_case döner, FE tarafında camelCase'e normalize edilir.

/** UI form values for creating an account */
export type CreateAccountFormValues = {
  customerId: string;
  currency: string; // e.g. "TRY"
};

/** Body for POST /v1/customers/{customerId}/accounts
 *  NOTE: Backend only expects { currency }, customerId is sent in path.
 */
export type CreateAccountBody = {
  customerId: string;
  currency: string;
};

/** Shared Account DTO used by both create and get */
export type AccountDTO = {
  id: string;
  currency: string;
  status: string; // ACTIVE | BLOCKED | CLOSED
};

/** Response of create account */
export type CreateAccountResponse = AccountDTO;

/** GET /v1/accounts?customerId=... returns a list of accounts */
export type AccountsListDTO = AccountDTO[];

/** Raw balance response from backend (snake_case) */
export type AccountBalanceRawDTO = {
  account_id: string;
  balance_minor: number;
  as_of_ledger_offset: number | null;
  updated_at: string | null;
};

/** UI-normalized balance DTO (camelCase) */
export type AccountBalanceDTO = {
  accountId: string;
  balanceMinor: number;
  asOfLedgerOffset: number | null;
  updatedAt: string | null;
};

/** Normalize raw response to UI DTO */
export function normalizeBalance(raw: AccountBalanceRawDTO): AccountBalanceDTO {
  return {
    accountId: raw.account_id,
    balanceMinor: raw.balance_minor,
    asOfLedgerOffset: raw.as_of_ledger_offset,
    updatedAt: raw.updated_at,
  };
}
