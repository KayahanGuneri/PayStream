/* Türkçe Özet:
   Yeni müşteri oluşturma sayfası. UI-only CreateCustomerForm bileşenini kompoze eder,
   I/O için useCreateCustomer hook’unu kullanır. Başarıda toast gösterir ve istenirse
   detay sayfasına yönlendirme yapar.
*/

import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import {CreateCustomerForm} from './Customers';
import type {CreateCustomerValues} from './Customers';
import { useCreateCustomer, extractFieldErrors } from '../hooks/useCustomers';
import { AuthError, ValidationError, ApiError } from '../lib/errors';

function getMessage(error: unknown, fallback = 'Unexpected error.'): string {
  // Safe message extraction from known error classes
  if (
    error instanceof AuthError ||
    error instanceof ValidationError ||
    error instanceof ApiError
  ) {
    return typeof (error as Error).message === 'string' ? (error as Error).message : fallback;
  }
  if (error instanceof Error && typeof error.message === 'string') return error.message;
  if (typeof error === 'object' && error && 'message' in error) {
    const m = (error as { message?: unknown }).message;
    if (typeof m === 'string') return m;
  }
  if (typeof error === 'string') return error;
  return fallback;
}

export const CustomersNewPage: React.FC = () => {
  const navigate = useNavigate();
  const createMutation = useCreateCustomer();

  const fieldErrors = useMemo(
    () => extractFieldErrors(createMutation.error),
    [createMutation.error]
  );

  const handleCreate = async (values: CreateCustomerValues) => {
    try {
      // Optional trimming/normalization
      const payload = {
        name: values.name.trim(),
        email: values.email.trim().toLowerCase(),
        password: values.password,
      };

      const created = await createMutation.mutateAsync(payload);
      toast.success('Customer created successfully.');
      // Navigate to details
      navigate(`/customers/${encodeURIComponent(created.id)}`);
    } catch (err: unknown) {
      if (err instanceof AuthError) {
        toast.error('Authentication/authorization required. (Redirect TODO)');
        return;
      }
      if (err instanceof ValidationError) {
        toast.warning(getMessage(err, 'Validation failed.'));
        return;
      }
      if (err instanceof ApiError) {
        toast.error(getMessage(err, 'Request failed.'));
        return;
      }
      toast.error(getMessage(err));
    }
  };

  return (
    <div className="space-y-8">
      {/* Hero */}
      <div className="overflow-hidden rounded-2xl border bg-white shadow-sm">
        <img
          src="/brand/ai-assist.png"
          alt="Customers Banner"
          className="h-40 w-full object-cover sm:h-56"
        />
        <div className="p-6">
          <h1 className="text-2xl font-bold">Create Customer</h1>
          <p className="mt-2 text-gray-600">
            Register a new customer to proceed with account creation and operations.
          </p>
        </div>
      </div>

      {/* UI-only form; IO comes from hook */}
      <CreateCustomerForm
        pending={createMutation.isPending}
        onSubmit={handleCreate}
        fieldErrors={fieldErrors}
        generalErrorMessage={
          createMutation.error instanceof ValidationError
            ? undefined
            : createMutation.error
            ? getMessage(createMutation.error, 'Request failed.')
            : undefined
        }
      />
    </div>
  );
};
