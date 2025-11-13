// src/pages/Accounts.tsx
// Türkçe Özet:
// Accounts sayfasının nihai sürümü. Üstte hero görseli, altında (varsa) tekil hesap listesi,
// bakiye kartı ve "Create Account" formu bulunur. Bilgilendirici Alert mesajları kaldırıldı.
// UI sadeleştirildi, işlev (create → detail → balance) akışı korunuyor.

import React, { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';

import { AccountsList } from '../features/accounts/List';
import { CreateForm } from '../features/accounts/CreateForm';
import type { CreateAccountFormValues } from '../types/accounts';

import { useCreateAccount, useAccount, useAccountBalance } from '../hooks/useAccounts';
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
  // Read URL params to optionally pre-fill customerId or show a specific account
  const [params, setParams] = useSearchParams();
  const customerIdFromUrl = params.get('customerId') ?? '';
  const accountIdParam = params.get('accountId') ?? '';

  // Hold the last created account id to drive detail + balance after creation
  const [lastCreatedId, setLastCreatedId] = useState<string>(accountIdParam);

  // Mutations & queries
  const createMutation = useCreateAccount(); // NOTE: your hook currently reads customerId from the submitted body
  const activeAccountId = lastCreatedId || accountIdParam;

  // Fetch account detail and balance only when we have an id
  const { data: account } = useAccount(activeAccountId);
  const { data: balance } = useAccountBalance(activeAccountId);

  // Compute per-field errors when create fails with 422
  const fieldErrors = useMemo(() => getFieldErrors(createMutation.error), [createMutation.error]);

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
        toast.error('Authentication/authorization required. (Redirect TODO)');
      } else if (err instanceof ValidationError) {
        toast.warning(getMessage(err, 'Validation failed.'));
      } else if (err instanceof ApiError) {
        toast.error(getMessage(err, 'Request failed.'));
      } else {
        toast.error(getMessage(err));
      }
    }
  };

  return (
    <div className="space-y-6">
      {/* Page hero */}
      <Hero
        imageSrc="/brand/banner-accounts.png"
        imageAlt="Accounts Banner"
        title="Accounts"
        subtitle="Create an account and inspect its details/balance."
      />

      {/* List area: show the single active account (if any), otherwise empty state */}
      {activeAccountId && account ? (
        <AccountsList
          accounts={[{ id: account.id, currency: account.currency, status: account.status }]}
          isLoading={false}
          error={undefined}
          onRefresh={undefined}
        />
      ) : (
        <EmptyState
          title="No accounts to show."
          description="Create a new account to see it listed here."
          imageSrc="/brand/account-service.png"
          actionLabel={undefined}
          onAction={undefined}
        />
      )}

      {/* Balance card (renders only when we have an active account) */}
      {activeAccountId && (
        <Card title="Balance">
          {balance ? (
            <div className="text-sm">
              <div>
                <span className="text-gray-500">Current:</span>{' '}
                <span className="font-semibold">
                  {formatMinor(balance.balanceMinor, account?.currency ?? 'TRY')}
                </span>
              </div>
              <div className="text-gray-500">
                As of offset: {balance.asOfLedgerOffset ?? 'Not available'} | Updated:{' '}
                {balance.updatedAt ?? 'Not available'}
              </div>
            </div>
          ) : (
            <div className="text-sm text-gray-600">Loading balance…</div>
          )}
        </Card>
      )}

      {/* Create form (UI-only) */}
      <CreateForm
        initialCustomerId={customerIdFromUrl}
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
