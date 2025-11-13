import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { http } from '../lib/http';
import { AuthError, ValidationError } from '../lib/errors';

// ---- DTOs (Data Transfer Objects) ----
export type AccountDTO = {
  id: string;
  currency: string;
  status: string;
};

export type CreateAccountBody = {
  // Customer ID is used in the request URL
  customerId: string;
  // Example: "TRY"
  currency: string;
};

export type CreateAccountResponse = AccountDTO;

export type AccountBalanceDTO = {
  accountId: string;
  currentBalance?: number;
  asOfLedgerOffset?: number;
  updatedAt?: string;
};

// Retry rule: Do not retry for 401, 403, 422 errors
const isAuthOrValidation = (err: unknown) =>
  err instanceof ValidationError || err instanceof AuthError;

/**
 * Create account
 * Backend endpoint: POST /v1/customers/{customerId}/accounts
 * Note: The `http` wrapper automatically prefixes all requests with `/api`
 */
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
      // Invalidate cache for the newly created account
      await qc.invalidateQueries({
        queryKey: ['accounts', 'byId', { accountId: data.id }],
      });

      // Invalidate cache for the account balance
      await qc.invalidateQueries({
        queryKey: ['accounts', 'balance', { accountId: data.id }],
      });

      // Future improvement: If a "list by customer" endpoint exists,
      // invalidate the list query for that customer as well.
    },

    // Retry only once for non-auth/validation errors
    retry: (count, err) => !isAuthOrValidation(err) && count < 1,
  });
}

/**
 * Fetch a single account
 * Backend endpoint: GET /v1/accounts/{accountId}
 */
export function useAccount(accountId: string) {
  return useQuery<AccountDTO, unknown>({
    queryKey: ['accounts', 'byId', { accountId }],
    enabled: !!accountId,

    queryFn: async () =>
      await http.get<AccountDTO>(
        `/v1/accounts/${encodeURIComponent(accountId)}`
      ),

    staleTime: 30_000, // Cache stays fresh for 30 seconds
    retry: (count, err) => !isAuthOrValidation(err) && count < 1,
  });
}

/**
 * Fetch account balance
 * Backend endpoint: GET /v1/accounts/{accountId}/balance
 */
export function useAccountBalance(accountId: string) {
  return useQuery<AccountBalanceDTO, unknown>({
    queryKey: ['accounts', 'balance', { accountId }],
    enabled: !!accountId,

    queryFn: async () =>
      await http.get<AccountBalanceDTO>(
        `/v1/accounts/${encodeURIComponent(accountId)}/balance`
      ),

    staleTime: 15_000, // Refresh balance every 15 seconds
    retry: (count, err) => !isAuthOrValidation(err) && count < 1,
  });
}
