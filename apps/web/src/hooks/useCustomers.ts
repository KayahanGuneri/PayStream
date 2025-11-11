/* Türkçe Özet:
   Customer I/O hook'ları. Müşteri oluşturma (POST /api/v1/customers) ve
   tekil müşteri sorgulama (GET /api/v1/customers/{id}) işlemlerini yönetir.
   401/403 → AuthError, 422 → ValidationError, diğerleri → ApiError olarak fırlatılır.
*/

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { http } from '../lib/http';
import { AuthError, ValidationError } from '../lib/errors';

// ---------- DTOs (FE projection) ----------
export type CustomerDTO = {
  id: string;
  name: string;
  email: string;
  // TODO: createdAt/updatedAt gibi alanlar varsa bildir.
};

export type CreateCustomerBody = {
  name: string;
  email: string;
  password: string;
};

export type CreateCustomerResponse = CustomerDTO;

// ---------- Helpers ----------
const isAuthOrValidation = (err: unknown) =>
  err instanceof ValidationError || err instanceof AuthError;

// Try to read field-level errors in two common shapes:
// { errors: {field: msg} } OR { fieldErrors: {field: msg} }
export function extractFieldErrors(error: unknown): Record<string, string> {
  if (error instanceof ValidationError) {
    const data = (error as ValidationError & { data?: unknown }).data as
      | { errors?: Record<string, string>; fieldErrors?: Record<string, string> }
      | undefined;
    if (data?.errors) return data.errors;
    if (data?.fieldErrors) return data.fieldErrors;
  }
  return {};
}

// ---------- Hooks ----------

// POST /api/v1/customers
export function useCreateCustomer() {
  const qc = useQueryClient();

  return useMutation<CreateCustomerResponse, unknown, CreateCustomerBody>({
    mutationKey: ['customers', 'create'],
    mutationFn: async (body) =>
      // NOTE: http automatically prefixes '/api'
      http.post<CreateCustomerResponse, CreateCustomerBody>('/v1/customers', body),
    onSuccess: async (data) => {
      // Invalidate any detail cache possibly referencing this id.
      await qc.invalidateQueries({ queryKey: ['customers', 'byId', { id: data.id }] });
    },
    retry: (count: number, err: unknown) => !isAuthOrValidation(err) && count < 1,
  });
}

// GET /api/v1/customers/{id}
export function useCustomer(id: string) {
  return useQuery<CustomerDTO, unknown>({
    queryKey: ['customers', 'byId', { id }],
    enabled: !!id, // Do not run without an id
    queryFn: async () => http.get<CustomerDTO>(`/v1/customers/${encodeURIComponent(id)}`),
    staleTime: 60_000, // Customer rarely changes
    retry: (count: number, err: unknown) => !isAuthOrValidation(err) && count < 1,
  });
}
