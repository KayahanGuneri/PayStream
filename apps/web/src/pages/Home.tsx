/* Türkçe Özet:
   Ana sayfa. Banner görüntüler, Accounts modülüne yönlendirme sunar.
*/
import React from 'react';
import { Link } from 'react-router-dom';

export const Home: React.FC = () => {
  return (
    <div className="space-y-8">
      {/* Hero */}
      <div className="overflow-hidden rounded-2xl border bg-white shadow-sm">
        <img
          src="/brand/banner-accounts.png"
          alt="PayStream Banner"
          className="h-48 w-full object-cover sm:h-64"
        />
        <div className="p-6">
          <h1 className="text-2xl font-bold">PayStream Web</h1>
          <p className="mt-2 text-gray-600">
            Event-driven banking modules with secure payments. Navigate to Accounts to create and
            inspect accounts via the API Gateway.
          </p>
          <div className="mt-4">
            <Link
              to="/accounts"
              className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700"
            >
              Go to Accounts
            </Link>
          </div>
        </div>
      </div>

      {/* Short modules teaser (static) */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[
          { title: 'Accounts', img: '/brand/account-service.png', href: '/accounts' },
          { title: 'Ledger', img: '/brand/ledger-service.png' },
          { title: 'Transfers', img: '/brand/transfer-service.png' },
          { title: 'Payments', img: '/brand/payment-service.png' },
        ].map((m) => (
          <div key={m.title} className="overflow-hidden rounded-xl border bg-white shadow-sm">
            <img src={m.img} alt={m.title} className="h-28 w-full object-cover" />
            <div className="p-4">
              <div className="text-sm font-semibold">{m.title}</div>
              {m.href ? (
                <Link to={m.href} className="mt-2 inline-block text-xs text-blue-600 hover:underline">
                  Open
                </Link>
              ) : (
                <span className="mt-2 inline-block text-xs text-gray-500">Coming soon</span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
