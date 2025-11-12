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
export function useCreateAccount() {
  const qc = useQueryClient();

  return useMutation<CreateAccountResponse, unknown, CreateAccountBody>({
    mutationKey: ['accounts', 'create'],
    mutationFn: async (body) => {
      return await http.post<CreateAccountResponse, { currency: string }>(
        `/v1/customers/${encodeURIComponent(body.customerId)}/accounts`,
        { currency: body.currency }
      );
    },
    onSuccess: async (data) => {
      await qc.invalidateQueries({ queryKey: ['accounts', 'byId', { accountId: data.id }] });
      await qc.invalidateQueries({ queryKey: ['accounts', 'balance', { accountId: data.id }] });
    },
    retry: (count, err) => !isAuthOrValidation(err) && count < 1
  });
}

/** Get account by id: GET /v1/accounts/{accountId} */
export function useAccount(accountId: string) {
  return useQuery<AccountDTO, unknown>({
    queryKey: ['accounts', 'byId', { accountId }],
    enabled: !!accountId,
    queryFn: async () => await http.get<AccountDTO>(`/v1/accounts/${encodeURIComponent(accountId)}`),
    staleTime: 30_000,
    retry: (count, err) => !isAuthOrValidation(err) && count < 1
  });
}

/** Get balance: GET /v1/accounts/{accountId}/balance */
export function useAccountBalance(accountId: string) {
  return useQuery<AccountBalanceDTO, unknown>({
    queryKey: ['accounts', 'balance', { accountId }],
    enabled: !!accountId,
    queryFn: async () =>
      await http.get<AccountBalanceDTO>(`/v1/accounts/${encodeURIComponent(accountId)}/balance`),
    staleTime: 15_000,
    retry: (count, err) => !isAuthOrValidation(err) && count < 1
  });
}
