// src/pages/__tests__/AccountsPage.auth-balance.test.tsx
// Türkçe Özet:
// Accounts sayfası için entegrasyon testleri. AuthError durumunda toast + /login
// yönlendirmesini ve balance widget'ın başarıyla formatlı değer göstermesini doğrular.

import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { AppProviders } from '../../providers/AppProviders';
import { Accounts } from '../Accounts';
import { http } from '../../lib/http';
import { AuthError } from '../../lib/errors';

// useNavigate'i mock'lamak için react-router-dom'u kısmi mock'larız
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async (orig) => {
  const actual = await orig<typeof import('react-router-dom')>();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

function renderAccounts(initialEntry = '/accounts?customerId=test-cid') {
  return render(
    <AppProviders>
      <MemoryRouter initialEntries={[initialEntry]}>
        <Accounts />
      </MemoryRouter>
    </AppProviders>
  );
}

describe('Accounts page - AuthError & Balance', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('redirects to /login on AuthError when creating account', async () => {
    // Create account çağrısında http.post → AuthError fırlatsın
    const postSpy = vi
      .spyOn(http, 'post')
      .mockRejectedValueOnce(new AuthError('Unauthorized', 401, null));

    renderAccounts('/accounts?customerId=test-cid');

    // Form alanlarını doldur
    const customerInput = await screen.findByLabelText(/Customer ID/i);
    const currencyInput = await screen.findByLabelText(/Currency/i);
    const submitButton = await screen.findByRole('button', { name: /Create Account/i });

    fireEvent.change(customerInput, { target: { value: 'test-cid' } });
    fireEvent.change(currencyInput, { target: { value: 'TRY' } });
    fireEvent.click(submitButton);

    // navigate('/login') beklentisi
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });

    expect(postSpy).toHaveBeenCalled();
  });

  it('shows formatted balance when balance endpoint succeeds', async () => {
    // Bu testte http.get'i doğrudan mock'layarak hem listeyi hem balance'ı kontrol ediyoruz.
    const getSpy = vi
      .spyOn(http, 'get')
      .mockImplementation(async (path: string): Promise<any> => {
        // Liste çağrısı: /v1/customers/{cid}/accounts
        if (path.startsWith('/v1/customers/')) {
          return [
            { id: 'acc-1', currency: 'TRY', status: 'ACTIVE' },
          ];
        }

        // Bakiye çağrısı: /v1/accounts/acc-1/balance
        if (path.startsWith('/v1/accounts/acc-1/balance')) {
          return {
            account_id: 'acc-1',
            balance_minor: 12345,
            as_of_ledger_offset: 10,
            updated_at: '2025-01-01T00:00:00Z',
          };
        }

        throw new Error(`Unexpected GET path in test: ${path}`);
      });

    renderAccounts('/accounts?customerId=test-cid');

    // "123.45 TRY" formatlanmış değeri balance kartında görmeliyiz
    await waitFor(() => {
      expect(screen.getByText('123.45 TRY')).toBeInTheDocument();
    });

    expect(getSpy).toHaveBeenCalled();
  });
});
