// Türkçe Özet:
// Account servisinin React Query hook'ları. Hesap oluşturma (create), listeleme (list),
// tekil hesap getirme (byId) ve bakiye sorgulama (balance) işlerini üstlenir.
// 401/403 → AuthError, 422 → ValidationError dışındaki hatalar → ApiError olarak ele alınır.

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { http } from '../lib/http';
import { AuthError, ValidationError } from '../lib/errors';
import type {
  AccountDTO,
  CreateAccountBody,
  CreateAccountResponse,
  AccountBalanceDTO,
  AccountBalanceRawDTO,
} from '../types/accounts';

// Simple alias for list response: backend returns an array of AccountDTO
export type AccountsListDTO = AccountDTO[];

// Retry rule: Do not retry for 401, 403, 422 errors
const isAuthOrValidation = (err: unknown) =>
  err instanceof ValidationError || err instanceof AuthError;

/**
 * Normalize raw balance response from snake_case to camelCase DTO.
 */
function mapBalance(raw: AccountBalanceRawDTO): AccountBalanceDTO {
  return {
    accountId: raw.account_id,
    balanceMinor: raw.balance_minor,
    asOfLedgerOffset: raw.as_of_ledger_offset,
    updatedAt: raw.updated_at,
  };
}

/**
 * List accounts for a given customer.
 * FE endpoint: GET /api/v1/accounts?customerId={id}
 * (Backend'te henüz yok, MSW ile simüle ediliyor.)
 */
export function useAccountsList(customerId: string) {
  return useQuery<AccountsListDTO, unknown>({
    queryKey: ['accounts', 'list', { customerId }],
    enabled: Boolean(customerId), // do not run without a customer id
    queryFn: async () =>
      await http.get<AccountsListDTO>(
        `/v1/accounts?customerId=${encodeURIComponent(customerId)}`
      ),
    staleTime: 15_000, // list can refresh a bit more often
    retry: (count, err) => !isAuthOrValidation(err) && count < 1,
  });
}

/**
 * Create account
 * Backend endpoint: POST /v1/customers/{customerId}/accounts
 * Note: The `http` wrapper automatically prefixes all requests with `/api`.
 */
export function useCreateAccount() {
  const qc = useQueryClient();

  return useMutation<CreateAccountResponse, unknown, CreateAccountBody>({
    mutationKey: ['accounts', 'create'],

    // The hook receives both customerId + currency,
    // but only currency is sent in the JSON body. customerId goes in the path.
    mutationFn: async (body) => {
      return await http.post<CreateAccountResponse, { currency: string }>(
        `/v1/customers/${encodeURIComponent(body.customerId)}/accounts`,
        { currency: body.currency }
      );
    },

    onSuccess: async (data, variables) => {
      // Invalidate detail cache for the newly created account
      await qc.invalidateQueries({
        queryKey: ['accounts', 'byId', { accountId: data.id }],
      });

      // Invalidate cache for the account balance
      await qc.invalidateQueries({
        queryKey: ['accounts', 'balance', { accountId: data.id }],
      });

      // If list hook is used with this customer, refresh it as well
      await qc.invalidateQueries({
        queryKey: ['accounts', 'list', { customerId: variables.customerId }],
      });
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
    enabled: Boolean(accountId), // do not run when id is empty

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
 * NOTE: Backend returns snake_case; we normalize it to camelCase.
 */
export function useAccountBalance(accountId: string) {
  return useQuery<AccountBalanceDTO, unknown>({
    queryKey: ['accounts', 'balance', { accountId }],
    enabled: Boolean(accountId),

    queryFn: async () => {
      const raw = await http.get<AccountBalanceRawDTO>(
        `/v1/accounts/${encodeURIComponent(accountId)}/balance`
      );
      return mapBalance(raw);
    },

    staleTime: 15_000, // Refresh balance every 15 seconds
    retry: (count, err) => !isAuthOrValidation(err) && count < 1,
  });
}
