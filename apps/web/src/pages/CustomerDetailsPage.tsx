/* Türkçe Özet:
   Tekil müşteri detay sayfası. URL parametresinden id okur, veriyi useCustomer
   ile yükler. "Bu müşteri için hesap oluştur" düğmesi sunar.
*/

import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useCustomer } from '../hooks/useCustomers';
import { CustomerDetails } from '../features/customers/Details';
import { ApiError } from '../lib/errors';

export const CustomerDetailsPage: React.FC = () => {
  // Read id from the route param
  const { id = '' } = useParams<{ id: string }>();
  const navigate = useNavigate();

  // Fetch customer by id
  const { data, isLoading, error } = useCustomer(id);

  return (
    <div className="space-y-8">
      {/* Hero */}
      <div className="overflow-hidden rounded-2xl border bg-white shadow-sm">
        <img
          src="/brand/account-service.png"
          alt="Customer Banner"
          className="h-40 w-full object-cover sm:h-56"
        />
        <div className="p-6">
          <h1 className="text-2xl font-bold">Customer</h1>
          <p className="mt-2 text-gray-600">Inspect customer details and proceed to accounts.</p>
        </div>
      </div>

      {/* Loading / Error states */}
      {isLoading && <div className="text-sm text-gray-600">Loading…</div>}

      {/* Use Boolean(error) to avoid placing 'unknown' in JSX */}
      {Boolean(error) && !(error instanceof ApiError) && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
          Unexpected error while loading customer.
        </div>
      )}

      {error instanceof ApiError && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
          {error.message ?? 'Request failed.'}
        </div>
      )}

      {/* Content */}
      {data && (
        <CustomerDetails
          customer={data}
          onCreateAccount={() => navigate(`/accounts?customerId=${encodeURIComponent(data.id)}`)}
        />
      )}
    </div>
  );
};
