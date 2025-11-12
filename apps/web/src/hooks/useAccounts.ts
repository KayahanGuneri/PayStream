import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { http } from '../lib/http';
import { AuthError, ValidationError } from '../lib/errors';

// ---- DTOs ----
export type AccountDTO = {
  id: string;
  currency: string;
  status: string;
};

export type CreateAccountBody = {
  customerId: string;  // URL içinde kullanılır
  currency: string;    // örn: "TRY"
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

/** Create account: POST /v1/customers/{customerId}/accounts */
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
    },
    retry: (count, err) => !isAuthOrValidation(err) && count < 1
  });
}

/** Get account by id: GET /v1/accounts/{accountId} */
/**
 * GET /v1/accounts/{accountId}
 */



// Tekil hesap GET /v1/accounts/{accountId}
 
export function useAccount(accountId: string) {
  return useQuery<AccountDTO, unknown>({
    queryKey: ['accounts', 'byId', { accountId }],
    enabled: !!accountId,
    queryFn: async () => await http.get<AccountDTO>(`/v1/accounts/${encodeURIComponent(accountId)}`),
    queryFn: async () =>
      await http.get<AccountDTO>(`/v1/accounts/${encodeURIComponent(accountId)}`),




      await http.get<AccountDTO>(`/api/v1/accounts/${encodeURIComponent(accountId)}`),
    staleTime: 30_000,
    retry: (count, err) => !isAuthOrValidation(err) && count < 1
  });
}


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
    retry: (count, err) => !isAuthOrValidation(err) && count < 1
  });
}
