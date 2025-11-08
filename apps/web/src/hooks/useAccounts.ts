/* Türkçe Özet:
   Accounts I/O hook'ları. Backend sözleşmesine göre '/v1' path'lerine istek atar.
   Liste endpoint'i henüz sağlanmadığı için create + byId + balance sağlanır.

   Accounts I/O: oluşturma, tekil okuma, bakiye. Liste uç noktası olmadığından
   list hook’u TODO olarak bırakıldı. Tüm istekler /api/v1 yolunu kullanır.
*/

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { http } from '../lib/http';
import { AuthError, ValidationError } from '../lib/errors';

// === DTOs (backend'e birebir uyum) ===


// ---- DTOs (FE projeksiyon) ----
export type AccountDTO = {
  id: string;
  currency: string;
  status: string;
};

export type CreateAccountBody = {
  customerId: string;     // used only to construct the URL
  currency: string;       // must be /^[A-Z]{3}$/ (e.g. TRY)

  // TODO: createdAt / iban vb. alanlar varsa belirt.
};

export type CreateAccountBody = {
  customerId: string;
  currency: string;
};

export type CreateAccountResponse = AccountDTO;

export type AccountBalanceDTO = {
  accountId: string;
  currentBalance?: number;
  asOfLedgerOffset?: number;
  updatedAt?: string;
};

// Retry guard: don't retry auth/validation
const isAuthOrValidation = (err: unknown) =>
  err instanceof ValidationError || err instanceof AuthError;

/**
 * POST /v1/customers/{customerId}/accounts
 */


// 401/403/422 tekrar deneme dışı
const isAuthOrValidation = (err: unknown) =>
  err instanceof ValidationError || err instanceof AuthError;

// export function useAccountsList(customerId: string) { ... }
 
export function useCreateAccount() {
  const qc = useQueryClient();

  return useMutation<CreateAccountResponse, unknown, CreateAccountBody>({
    mutationKey: ['accounts', 'create'],
    mutationFn: async (body) => {
      // IMPORTANT: http.ts already prefixes '/api', so we only pass '/v1/...'
      return await http.post<CreateAccountResponse, { currency: string }>(
        `/v1/customers/${encodeURIComponent(body.customerId)}/accounts`,


      // Backend sözleşmesi: POST /v1/customers/{customerId}/accounts
      return await http.post<CreateAccountResponse, { currency: string }>(
        `/api/v1/customers/${encodeURIComponent(body.customerId)}/accounts`,

        { currency: body.currency }
      );
    },
    onSuccess: async (data) => {
      // Invalidate caches that might contain this account
      await qc.invalidateQueries({ queryKey: ['accounts', 'byId', { accountId: data.id }] });
      await qc.invalidateQueries({ queryKey: ['accounts', 'balance', { accountId: data.id }] });
      // Future: if a "list by customer" exists, also invalidate:


      // Liste yok, yine de tekil account ve balance cache’lerini tazeleyebiliriz.
      await qc.invalidateQueries({ queryKey: ['accounts', 'byId', { accountId: data.id }] });
      await qc.invalidateQueries({ queryKey: ['accounts', 'balance', { accountId: data.id }] });
      // İleride "list by customer" gelirse şunu ekleriz:

      // await qc.invalidateQueries({ queryKey: ['accounts', 'list', { customerId: variables.customerId }] });
    },
    retry: (count: number, err: unknown) => !isAuthOrValidation(err) && count < 1,
  });
}

/**
 * GET /v1/accounts/{accountId}
 */


// Tekil hesap GET /v1/accounts/{accountId}
 
export function useAccount(accountId: string) {
  return useQuery<AccountDTO, unknown>({
    queryKey: ['accounts', 'byId', { accountId }],
    enabled: !!accountId,
    queryFn: async () =>
      await http.get<AccountDTO>(`/v1/accounts/${encodeURIComponent(accountId)}`),


      await http.get<AccountDTO>(`/api/v1/accounts/${encodeURIComponent(accountId)}`),
    staleTime: 30_000,
    retry: (count: number, err: unknown) => !isAuthOrValidation(err) && count < 1,
  });
}

/**
 * GET /v1/accounts/{accountId}/balance
 */

// Bakiye GET /v1/accounts/{accountId}/balance

export function useAccountBalance(accountId: string) {
  return useQuery<AccountBalanceDTO, unknown>({
    queryKey: ['accounts', 'balance', { accountId }],
    enabled: !!accountId,
    queryFn: async () =>
      await http.get<AccountBalanceDTO>(`/v1/accounts/${encodeURIComponent(accountId)}/balance`),


      await http.get<AccountBalanceDTO>(
        `/api/v1/accounts/${encodeURIComponent(accountId)}/balance`
      ),
    staleTime: 15_000,
    retry: (count: number, err: unknown) => !isAuthOrValidation(err) && count < 1,
  });
}
