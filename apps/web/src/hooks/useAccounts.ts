// Türkçe Özet:
// Account servisinin React Query hook'ları. Hesap oluşturma, listeleme,
// tekil hesap getirme ve bakiye sorgulama işlevlerini içerir.
// 401/403 → AuthError, 422 → ValidationError dışındaki hatalar → ApiError.

// English inline summary:
// React Query hooks for Account operations (list, create, detail, balance).
// 401/403 → AuthError, 422 → ValidationError. All other non-2xx → ApiError.
// Account servisinin React Query hook'ları. Hesap oluşturma (create), listeleme (list),
// tekil hesap getirme (byId) ve bakiye sorgulama (balance) işlerini üstlenir.
// 401/403 → AuthError, 422 → ValidationError dışındaki hatalar → ApiError olarak ele alınır.

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { http } from '../lib/http';
import { AuthError, ValidationError } from '../lib/errors';
import type {
  AccountDTO,
  AccountsListDTO,
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
 * List accounts for a given customer
 * Backend endpoint: GET /v1/customers/{customerId}/accounts
 * List accounts for a given customer.
 * FE endpoint: GET /api/v1/accounts?customerId={id}
 * (Backend'te henüz yok, MSW ile simüle ediliyor.)
 */
export function useAccountsList(customerId: string) {
  return useQuery<AccountsListDTO, unknown>({
    queryKey: ['accounts', 'list', { customerId }],
    enabled: Boolean(customerId),
    queryFn: async () =>
      await http.get<AccountsListDTO>(
        `/v1/customers/${encodeURIComponent(customerId)}/accounts`
      ),
    staleTime: 15_000,

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
      const url = `/v1/customers/${encodeURIComponent(body.customerId)}/accounts`;
      return await http.post<CreateAccountResponse, { currency: string }>(url, {
        currency: body.currency,
      });
    },

    // Invalidate detail + balance + list on success
    onSuccess: async (data, variables) => {

    onSuccess: async (data, variables) => {
      // Invalidate detail cache for the newly created account
      await qc.invalidateQueries({
        queryKey: ['accounts', 'byId', { accountId: data.id }],
      });

      await qc.invalidateQueries({
        queryKey: ['accounts', 'balance', { accountId: data.id }],
      });


      // If list hook is used with this customer, refresh it as well
      await qc.invalidateQueries({
        queryKey: ['accounts', 'list', { customerId: variables.customerId }],
      });
    },

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
    enabled: Boolean(accountId),

    enabled: Boolean(accountId), // do not run when id is empty

    queryFn: async () => {
      const url = `/v1/accounts/${encodeURIComponent(accountId)}`;
      return await http.get<AccountDTO>(url);
    },

    staleTime: 30_000,
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
      const url = `/v1/accounts/${encodeURIComponent(accountId)}/balance`;
      const raw = await http.get<AccountBalanceRawDTO>(url);

      const raw = await http.get<AccountBalanceRawDTO>(
        `/v1/accounts/${encodeURIComponent(accountId)}/balance`
      );
      return mapBalance(raw);
    },

    staleTime: 15_000,
    retry: (count, err) => !isAuthOrValidation(err) && count < 1,
  });
}
