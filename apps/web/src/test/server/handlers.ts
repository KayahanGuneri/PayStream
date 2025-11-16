// Türkçe Özet: Account MSW handler'ları. Liste, tekil hesap ve bakiye uçları
// FE DTO'ları ile bire bir hizalıdır. Minimal bellek içi DB simülasyonu içerir.

import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

type DBAccount = { id: string; currency: string; status: string };
type DBBalance = {
  account_id: string;
  balance_minor: number;
  as_of_ledger_offset: number | null;
  updated_at: string | null;
};

// In-memory DB
const db = {
  accounts: new Map<string, DBAccount>(),
  balances: new Map<string, DBBalance>()
};

let counter = 1;

// --- Handlers ---
const handlers = [
  /** ➤ POST /v1/customers/{cid}/accounts */
  http.post('/api/v1/customers/:cid/accounts', async ({ params, request }) => {
    const body = (await request.json()) as { currency?: string };
    if (!body?.currency) {
      return HttpResponse.json({ errors: { currency: 'Currency is required' } }, { status: 422 });
    }
    const id = `acc-${counter++}`;
    const acc: DBAccount = { id, currency: body.currency.toUpperCase(), status: 'ACTIVE' };
    db.accounts.set(id, acc);
    db.balances.set(id, {
      account_id: id,
      balance_minor: 0,
      as_of_ledger_offset: 0,
      updated_at: new Date().toISOString()
    });
    return HttpResponse.json(acc, { status: 200 });
  }),

  /** ➤ GET /v1/accounts?customerId=... */
  http.get('/api/v1/accounts', ({ request }) => {
    const url = new URL(request.url);
    const customerId = url.searchParams.get('customerId');
    // MSW test ortamında müşteri-ilişkisi tutmuyoruz → tüm hesapları dönüyoruz
    const list = Array.from(db.accounts.values());
    return HttpResponse.json(list, { status: 200 });
  }),

  /** ➤ GET /v1/accounts/{id} */
  http.get('/api/v1/accounts/:id', ({ params }) => {
    const id = String(params.id);
    const acc = db.accounts.get(id);
    if (!acc) return new HttpResponse('Not found', { status: 404 });
    return HttpResponse.json(acc, { status: 200 });
  }),

  /** ➤ GET /v1/accounts/{id}/balance */
  http.get('/api/v1/accounts/:id/balance', ({ params }) => {
    const id = String(params.id);
    const bal = db.balances.get(id);
    if (!bal) return new HttpResponse('Not found', { status: 404 });
    return HttpResponse.json(bal, { status: 200 });
  })
];

export const server = setupServer(...handlers);
