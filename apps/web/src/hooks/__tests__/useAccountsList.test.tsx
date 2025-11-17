// src/hooks/__tests__/useAccountsList.test.tsx
// Türkçe Özet:
// useAccountsList hook'unu test eder. Success, empty ve error senaryolarını
// http.get üzerinde spy/mock kullanarak doğrular.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import { http } from '../../lib/http';
import { useAccountsList } from '../useAccounts';
// Basit bir probe komponenti: hook çıktısını DOM'a yazar
const TestProbe: React.FC<{ customerId: string }> = ({ customerId }) => {
  const { data, isLoading, error } = useAccountsList(customerId);

  if (isLoading) {
    return <div data-testid="status">loading</div>;
  }

  if (error) {
    return <div data-testid="status">error</div>;
  }

  return (
    <div data-testid="status">
      {Array.isArray(data) ? `count:${data.length}` : 'no-data'}
    </div>
  );
};

function renderWithClient(ui: React.ReactElement) {
  const client = new QueryClient({
    defaultOptions: {
      queries: {
        retry: 0,
        refetchOnWindowFocus: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={client}>
      {ui}
    </QueryClientProvider>
  );
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe('useAccountsList', () => {
  it('returns list on success (non-empty)', async () => {
    const getSpy = vi
      .spyOn(http, 'get')
      .mockResolvedValueOnce([
        { id: 'acc-1', currency: 'TRY', status: 'ACTIVE' },
        { id: 'acc-2', currency: 'USD', status: 'BLOCKED' },
      ] as any);

    renderWithClient(<TestProbe customerId="demo-cid" />);

    await waitFor(() => {
      expect(screen.getByTestId('status').textContent).toBe('count:2');
    });

    // Hook'ta http.get sadece path ile çağrılıyor, ikinci argüman yok
    expect(getSpy).toHaveBeenCalledWith('/v1/customers/demo-cid/accounts');
  });

  it('returns empty list when backend responds with []', async () => {
    const getSpy = vi.spyOn(http, 'get').mockResolvedValueOnce([] as any);

    renderWithClient(<TestProbe customerId="demo-cid" />);

    await waitFor(() => {
      expect(screen.getByTestId('status').textContent).toBe('count:0');
    });

    expect(getSpy).toHaveBeenCalled();
  });

  it('exposes error when request fails', async () => {
    // Tüm çağrılar hata versin (retry sürecinde de hep hata alacak)
    const getSpy = vi
      .spyOn(http, 'get')
      .mockRejectedValue(new Error('boom'));

    renderWithClient(<TestProbe customerId="demo-cid" />);

    await waitFor(
      () => {
        expect(screen.getByTestId('status').textContent).toBe('error');
      },
      { timeout: 4000 } // retry delay + ekstra pay
    );

    expect(getSpy).toHaveBeenCalled();
  });
});
