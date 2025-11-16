// Türkçe Özet:
// Accounts sayfasının nihai sürümü. Üstte hero görseli, altında seçili müşteri için
// hesap listesi, bakiye kartı ve "Create Account" formu bulunur. Liste gerçek
// endpoint üzerinden gelir; loading skeleton, empty state ve error + retry desteği vardır.
// AuthError durumunda toast gösterilir ve /login sayfasına yönlendirilir.

import React, { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';

import { AccountsList } from '../features/accounts/List';
import { CreateForm } from '../features/accounts/CreateForm';
import type { CreateAccountFormValues } from '../types/accounts';

import {
  useCreateAccount,
  useAccountBalance,
  useAccountsList,
} from '../hooks/useAccounts';
import { AuthError, ValidationError, ApiError } from '../lib/errors';

import { Hero } from '../components/Hero';
import { Card } from '../components/Card';
import { EmptyState } from '../components/EmptyState';

// Safely extract a human-readable message from various error shapes
function getMessage(error: unknown, fallback = 'Unexpected error.'): string {
  if (error instanceof AuthError || error instanceof ValidationError || error instanceof ApiError) {
    return typeof error.message === 'string' ? error.message : fallback;
  }
  if (error instanceof Error && typeof error.message === 'string') return error.message;
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const msg = (error as { message?: unknown }).message;
    if (typeof msg === 'string') return msg;
  }
  if (typeof error === 'string') return error;
  return fallback;
}

// Read per-field errors from a ValidationError.data payload (supports {errors} or {fieldErrors})
function getFieldErrors(error: unknown): Record<string, string> {
  if (error instanceof ValidationError) {
    const d = (error as ValidationError & { data?: unknown }).data;
    if (d && typeof d === 'object') {
      const obj = d as Record<string, unknown>;
      if ('errors' in obj && typeof obj.errors === 'object' && obj.errors) {
        return obj.errors as Record<string, string>;
      }
      if ('fieldErrors' in obj && typeof obj.fieldErrors === 'object' && obj.fieldErrors) {
        return obj.fieldErrors as Record<string, string>;
      }
    }
  }
  return {};
}

// Helper: convert MINOR units to major with 2 decimals (e.g., 12345 → 123.45)
function formatMinor(amountMinor: number | undefined | null, currency: string): string {
  if (amountMinor == null) return '-';
  const major = amountMinor / 100;
  return `${major.toFixed(2)} ${currency}`;
}

export const Accounts: React.FC = () => {
  const navigate = useNavigate();

  // Read URL params to optionally pre-fill customerId or show a specific account
  const [params, setParams] = useSearchParams();
  const customerIdFromUrl = params.get('customerId') ?? '';
  const accountIdParam = params.get('accountId') ?? '';

  // Hold the last created account id to drive balance after creation
  const [lastCreatedId, setLastCreatedId] = useState<string>(accountIdParam);

  const hasCustomer = Boolean(customerIdFromUrl);

  // Queries & mutations
  const listQuery = useAccountsList(customerIdFromUrl);
  const createMutation = useCreateAccount();

  // Decide which account is "active" for balance widget
  const activeAccountId =
    lastCreatedId ||
    accountIdParam ||
    (listQuery.data && listQuery.data.length > 0 ? listQuery.data[0].id : '');

  const balanceQuery = useAccountBalance(activeAccountId);

  // Compute per-field errors when create fails with 422
  const fieldErrors = useMemo(
    () => getFieldErrors(createMutation.error),
    [createMutation.error]
  );

  // Create handler wires the UI-only form to the mutation
  const handleCreate = async (values: CreateAccountFormValues) => {
    try {
      if (!values.customerId) {
        toast.warning('Please enter a valid customerId.');
        return;
      }

      // Uppercase currency to keep a clean convention (TRY, USD, EUR, ...)
      const res = await createMutation.mutateAsync({
        customerId: values.customerId,
        currency: values.currency.toUpperCase(),
      });

      toast.success('Account created.');
      setLastCreatedId(res.id);

      // Deep-link convenience: reflect newly created account + customer in URL
      const next = new URLSearchParams(params);
      next.set('accountId', res.id);
      next.set('customerId', values.customerId);
      setParams(next, { replace: true });
    } catch (err: unknown) {
      if (err instanceof AuthError) {
        // AuthError → toast + redirect to /login
        toast.error('Authentication/authorization required. Redirecting to login…');
        navigate('/login');
        return;
      }
      if (err instanceof ValidationError) {
        toast.warning(getMessage(err, 'Validation failed.'));
      } else if (err instanceof ApiError) {
        toast.error(getMessage(err, 'Request failed.'));
      } else {
        toast.error(getMessage(err));
      }
    }
  };

  // General error message for CreateForm (non-422)
  const generalErrorMessage =
    createMutation.error instanceof ValidationError
      ? undefined
      : createMutation.error
      ? getMessage(createMutation.error, 'Request failed.')
      : undefined;

  return (
    <div className="space-y-6">
      {/* Page hero */}
      <Hero
        imageSrc="/brand/banner-accounts.png"
        imageAlt="Accounts Banner"
        title="Accounts"
        subtitle="Create an account and inspect its details and balance."
      />

      {/* If no customerId is provided, guide the user instead of hitting the list endpoint */}
      {!hasCustomer && (
        <EmptyState
          title="No customer selected."
          description="Open a customer from the Customers page and use the 'Create Account' button, or pass ?customerId=... in the URL."
          imageSrc="/brand/account-service.png"
        />
      )}

      {/* List area: only when we have a customerId */}
      {hasCustomer && (
        <AccountsList
          accounts={listQuery.data ?? []}
          isLoading={listQuery.isLoading}
          error={listQuery.error}
          onRefresh={listQuery.refetch}
        />
      )}

      {/* Balance card (renders only when we have an active account id) */}
      {activeAccountId && (
        <Card title="Balance">
          {balanceQuery.isLoading && (
            <div className="text-sm text-gray-600">Loading balance…</div>
          )}

          {!!balanceQuery.error && !balanceQuery.isLoading && (
            <div className='text-sm text-red-700'>
              Failed to load balance.Plase try again later.
            </div>
          )}

          {balanceQuery.data && !balanceQuery.isLoading && !balanceQuery.error && (
            <div className="text-sm">
              <div>
                <span className="text-gray-500">Current:</span>{' '}
                <span className="font-semibold">
                  {formatMinor(
                    balanceQuery.data.balanceMinor,
                    listQuery.data?.find((a) => a.id === activeAccountId)?.currency ?? 'TRY'
                  )}
                </span>
              </div>
              <div className="text-gray-500">
                As of offset:{' '}
                {balanceQuery.data.asOfLedgerOffset ?? 'Not available'} | Updated:{' '}
                {balanceQuery.data.updatedAt ?? 'Not available'}
              </div>
            </div>
          )}
        </Card>
      )}

      {/* Create form (UI-only) */}
      <CreateForm
        initialCustomerId={customerIdFromUrl}
        pending={createMutation.isPending}
        onSubmit={handleCreate}
        fieldErrors={fieldErrors}
        generalErrorMessage={generalErrorMessage}
      />
    </div>
  );
};
