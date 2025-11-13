// Türkçe Özet:
// MSW handler'ları: accounts create/detail/balance uçları için minimal stub.
// DTO'lar FE'de kullanılan minimum alanlarla eşleşir.

import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

// Minimal in-memory store for tests
const db = {
  accounts: new Map<string, { id: string; currency: string; status: string }>(),
  balances: new Map<string, { account_id: string; balance_minor: number; as_of_ledger_offset?: number; updated_at?: string }>(),
};

let counter = 1;

const handlers = [
  // POST /v1/customers/{cid}/accounts
  http.post('/api/v1/customers/:cid/accounts', async ({ params, request }) => {
    const body = (await request.json()) as { currency?: string };
    if (!body?.currency) {
      return HttpResponse.json({ errors: { currency: 'Required' } }, { status: 422 });
    }
    const id = `acc-${counter++}`;
    const acc = { id, currency: String(body.currency).toUpperCase(), status: 'ACTIVE' };
    db.accounts.set(id, acc);
    db.balances.set(id, { account_id: id, balance_minor: 0, as_of_ledger_offset: 0, updated_at: new Date().toISOString() });
    return HttpResponse.json(acc, { status: 200 });
  }),

  // GET /v1/accounts/{id}
  http.get('/api/v1/accounts/:id', ({ params }) => {
    const id = String(params.id);
    const acc = db.accounts.get(id);
    if (!acc) return new HttpResponse('Not found', { status: 404 });
    return HttpResponse.json(acc, { status: 200 });
  }),

  // GET /v1/accounts/{id}/balance
  http.get('/api/v1/accounts/:id/balance', ({ params }) => {
    const id = String(params.id);
    const bal = db.balances.get(id);
    if (!bal) return new HttpResponse('Not found', { status: 404 });
    return HttpResponse.json(bal, { status: 200 });
  }),
];

export const server = setupServer(...handlers);
