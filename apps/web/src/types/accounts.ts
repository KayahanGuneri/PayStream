/* Türkçe Özet:
   Accounts modülü için ortak tipler. Form değerleri, istek/yanıt DTO’ları
   gibi tipler burada tutulur ve UI/hook/feature katmanları buradan kullanır.
*/

// Form values used by Create Account UI
export type CreateAccountFormValues = {
  customerId: string;
  currency: string; // e.g., "TRY" (3-letter uppercase)
};

// (Opsiyonel) Hook'lar ile uyumlu olması için ekleyebiliriz.
// FE tarafında POST body genelde form ile aynı.
export type CreateAccountBody = CreateAccountFormValues;

// (Opsiyonel) Liste/yanıt projeksiyonları — backend’den teyit bekler.
export type AccountDTO = {
  id: string;
  currency: string;
  status: string; // e.g., ACTIVE | BLOCKED | CLOSED
  // TODO: createdAt / iban gibi alanlar var mı?
};

export type AccountBalanceDTO = {
  accountId: string;
  currentBalance?: number;
  asOfLedgerOffset?: number;
  updatedAt?: string;
  // TODO: minor/major dönüşümü gerekirse belirt.
};
