// src/features/accounts/List.tsx
// Türkçe Özet:
// Accounts UI listesi. Veri üst katmandan props ile gelir; fetch yapmaz.
// Loading skeleton, empty state ve error+retry görselleri içerir.

import React from 'react';
import { Card } from '../../components/Card';
import { Button } from '../../components/Button';
import { EmptyState } from '../../components/EmptyState';

export type AccountsListProps = {
  accounts: Array<{ id: string; currency: string; status: string }>;
  isLoading?: boolean;
  error?: unknown;
  onRefresh?: () => void;
};

export const AccountsList: React.FC<AccountsListProps> = ({
  accounts,
  isLoading = false,
  error,
  onRefresh,
}) => {
  // Loading skeleton (gentle)
  if (isLoading) {
    return (
      <Card title="Accounts">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div
              key={i}
              className="rounded-xl border border-gray-200 bg-gray-50 p-4 shadow-sm"
              aria-busy="true"
            >
              <div className="h-4 w-1/2 animate-pulse rounded bg-gray-200" />
              <div className="mt-2 h-3 w-1/3 animate-pulse rounded bg-gray-200" />
              <div className="mt-4 h-8 w-full animate-pulse rounded bg-gray-200" />
            </div>
          ))}
        </div>
      </Card>
    );
  }

  // Error view with retry
  if (error) {
    return (
      <Card title="Accounts">
        <div className="flex items-center justify-between">
          <div className="text-sm text-red-700">
            Failed to load accounts. You can try again.
          </div>
          {onRefresh && (
            <Button variant="outline" onClick={onRefresh}>
              Retry
            </Button>
          )}
        </div>
      </Card>
    );
  }

  // Empty state
  if (!accounts || accounts.length === 0) {
    return (
      <EmptyState
        title="No accounts to show."
        description="Create a new account to see it listed here."
        imageSrc="/brand/account-service.png"
      />
    );
  }

  // Normal grid/table
  return (
    <Card title="Accounts">
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {accounts.map((acc) => (
          <div key={acc.id} className="rounded-xl border bg-white p-4 shadow-sm">
            <div className="text-sm font-semibold">{acc.id}</div>
            <div className="mt-1 text-sm text-gray-600">
              {acc.currency} • {acc.status}
            </div>
            <div className="mt-3">
              <Button
                variant="ghost"
                onClick={() => {
                  // noop here; page-level can handle deep-linking by setting URL ?accountId=...
                  // This component stays UI-only.
                  const usp = new URLSearchParams(window.location.search);
                  usp.set('accountId', acc.id);
                  window.history.replaceState(null, '', `${window.location.pathname}?${usp}`);
                }}
              >
                Open
              </Button>
            </div>
          </div>
        ))}
      </div>
    </Card>
  );
};
