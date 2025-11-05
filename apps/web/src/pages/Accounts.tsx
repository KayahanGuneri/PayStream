/* Türkçe Özet:
   Accounts sayfası; hero, query’den customerId/accountId okur.
   Liste uç noktası olmadığı için bilgilendirme gösterir.
   Create sonrası dönen accountId ile tekil hesap ve bakiye görüntülenir.
*/

import React, { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';
import { AccountsList } from '../features/accounts/List';
import { CreateForm } from '../features/accounts/CreateForm';
import type { CreateAccountFormValues } from '../types/accounts';
import { useCreateAccount, useAccount, useAccountBalance } from '../hooks/useAccounts';
import { AuthError, ValidationError, ApiError } from '../lib/errors';

// Safe message extractor
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

function getFieldErrors(error: unknown): Record<string, string> {
  if (error instanceof ValidationError) {
    const d = (error as ValidationError & { data?: unknown }).data;
    if (d && typeof d === 'object') {
      const obj = d as Record<string, unknown>;
      if ('errors' in obj) {
        const maybeErrors = obj['errors'];
        if (maybeErrors && typeof maybeErrors === 'object') {
          return maybeErrors as Record<string, string>;
        }
      }
      if ('fieldErrors' in obj) {
        const maybeFieldErrors = obj['fieldErrors'];
        if (maybeFieldErrors && typeof maybeFieldErrors === 'object') {
          return maybeFieldErrors as Record<string, string>;
        }
      }
    }
  }
  return {};
}

export const Accounts: React.FC = () => {
  const [params, setParams] = useSearchParams();
  const customerId = params.get('customerId') ?? '';
  const accountIdParam = params.get('accountId') ?? '';

  // After create, hold the returned id so we can show it
  const [lastCreatedId, setLastCreatedId] = useState<string>(accountIdParam);

  // Create mutation
  const createMutation = useCreateAccount();

  // Load single account + balance if we have an id (from query or last created)
  const activeAccountId = lastCreatedId || accountIdParam;
  const { data: account } = useAccount(activeAccountId);
  const { data: balance } = useAccountBalance(activeAccountId);

  const fieldErrors = useMemo(() => getFieldErrors(createMutation.error), [createMutation.error]);

  const handleCreate = async (values: CreateAccountFormValues) => {
    try {
      const res = await createMutation.mutateAsync({
        customerId: values.customerId,
        currency: values.currency,
      });
      toast.success('Account created.');
      setLastCreatedId(res.id);

      // put accountId to the URL for deep-linking convenience
      const next = new URLSearchParams(params);
      next.set('accountId', res.id);
      setParams(next, { replace: true });
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
          src="/brand/banner-accounts.png"
          alt="Accounts Banner"
          className="h-48 w-full object-cover sm:h-64"
        />
        <div className="p-6">
          <h1 className="text-2xl font-bold">Accounts</h1>
          <p className="mt-2 text-gray-600">Create an account and inspect its details/balance.</p>
        </div>
      </div>

      {/* No list endpoint info */}
      <div className="rounded-xl border bg-amber-50 p-4 text-sm text-amber-900">
        <strong>Note:</strong> Backend doesn’t expose “list by customer” yet. You can create an
        account and then view its details/balance. If you already have an account, use{' '}
        <code>?accountId=&lt;uuid&gt;</code> in the URL.
      </div>

      {/* Optional hint for missing customerId */}
      {!customerId && (
        <div className="rounded-xl border bg-yellow-50 p-4 text-sm text-yellow-900">
          No <code>customerId</code> specified. Add <code>?customerId=&lt;uuid&gt;</code> to create
          an account for a specific customer.
        </div>
      )}

      {/* “List” area shows either the created account or the account from the query */}
      <AccountsList
        accounts={
          activeAccountId && account
            ? [{ id: account.id, currency: account.currency, status: account.status }]
            : []
        }
        isLoading={false}
        error={undefined}
        onRefresh={undefined}
      />

      {/* Balance (if accountId available) */}
      {activeAccountId && (
        <div className="rounded-xl border bg-white p-6 shadow-sm">
          <h3 className="mb-2 text-base font-semibold">Balance</h3>
          {balance ? (
            <div className="text-sm">
              <div>
                <span className="text-gray-500">Current:</span>{' '}
                <span className="font-semibold">{balance.currentBalance ?? 0}</span>
              </div>
              <div className="text-gray-500">
                As of offset: {balance.asOfLedgerOffset ?? '-'} | Updated:{' '}
                {balance.updatedAt ?? '-'}
              </div>
            </div>
          ) : (
            <div className="text-sm text-gray-600">Loading balance…</div>
          )}
        </div>
      )}

      {/* Create form */}
      <CreateForm
        initialCustomerId={customerId}
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
