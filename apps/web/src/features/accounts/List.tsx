/* Türkçe Özet:
   Hesap listesini gösteren saf UI bileşeni. Veri çekmez; props olarak aldığı
   account listesini, yükleme ve hata durumlarını gösterir. Responsive grid
   veya tablo biçiminde çalışır.
*/

import React from 'react';
import { ApiError } from '../../lib/errors';

// DTO interface (frontend-side projection)
export type AccountDTO = {
  id: string;
  currency: string;
  status: string; // e.g., ACTIVE | BLOCKED | CLOSED
  // TODO: Add extra fields when backend returns them (createdAt, iban, etc.)
};

export type AccountsListProps = {
  accounts: AccountDTO[];
  isLoading?: boolean;
  error?: ApiError;
  onRefresh?: () => void;
};

export const AccountsList: React.FC<AccountsListProps> = ({
  accounts,
  isLoading,
  error,
  onRefresh,
}) => {
  // Loading skeleton
  if (isLoading) {
    return (
      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold">Accounts</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-24 animate-pulse rounded-lg bg-gray-100" />
          ))}
        </div>
      </div>
    );
  }

  // Error inline message (non-blocking)
  const inlineError = error ? (
    <div className="mb-3 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
      {error.message ?? 'Failed to load accounts.'}
    </div>
  ) : null;

  // Empty state
  if (!accounts || accounts.length === 0) {
    return (
      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold">Accounts</h2>
        {inlineError}
        <div className="rounded-md border border-dashed p-8 text-center text-sm text-gray-600">
          No accounts found for this customer.
          {onRefresh && (
            <div className="mt-3">
              <button
                onClick={onRefresh}
                className="rounded-md border bg-white px-3 py-1.5 text-sm shadow-sm hover:bg-gray-50"
              >
                Refresh
              </button>
            </div>
          )}
        </div>
      </div>
    );
  }

  // Success view
  return (
    <div className="rounded-xl border bg-white p-6 shadow-sm">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-lg font-semibold">Accounts</h2>
        {onRefresh && (
          <button
            onClick={onRefresh}
            className="rounded-md border bg-white px-3 py-1.5 text-sm shadow-sm hover:bg-gray-50"
          >
            Refresh
          </button>
        )}
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {accounts.map((acc) => (
          <div
            key={acc.id}
            className="flex flex-col justify-between rounded-lg border p-4 transition hover:-translate-y-0.5 hover:shadow"
          >
            <div>
              <div className="text-sm text-gray-500">Account ID</div>
              <div className="truncate text-sm font-mono text-gray-800">{acc.id}</div>
            </div>

            <div className="mt-3 flex items-center justify-between">
              <div>
                <div className="text-xs text-gray-500">Currency</div>
                <div className="text-base font-semibold">{acc.currency}</div>
              </div>

              {/* Status badge */}
              <span
                className={
                  'inline-flex items-center rounded-full px-2 py-0.5 text-xs ' +
                  (acc.status === 'ACTIVE'
                    ? 'bg-emerald-100 text-emerald-700'
                    : acc.status === 'BLOCKED'
                    ? 'bg-amber-100 text-amber-700'
                    : 'bg-gray-200 text-gray-700')
                }
                title={`Status: ${acc.status}`}
              >
                {acc.status}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
