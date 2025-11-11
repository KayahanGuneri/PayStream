/* Türkçe Özet:
   Müşteri detaylarının sade gösterimi. UI-only; dışarıdan gelen veriyi basar,
   "Hesap oluştur" aksiyonunu parent’a callback ile bildirir.
*/

import React from 'react';
import type { CustomerDTO } from '../../hooks/useCustomers';

type Props = {
  customer: CustomerDTO;
  onCreateAccount: () => void;
};

export const CustomerDetails: React.FC<Props> = ({ customer, onCreateAccount }) => {
  // Pure presentational component; no side effects
  return (
    <div className="rounded-xl border bg-white p-6 shadow-sm">
      <div className="mb-4">
        <div className="text-sm text-gray-500">Customer ID</div>
        <div className="font-mono text-sm">{customer.id}</div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <div className="text-sm text-gray-500">Name</div>
          <div className="font-medium">{customer.name}</div>
        </div>
        <div>
          <div className="text-sm text-gray-500">Email</div>
          <div className="font-medium">{customer.email}</div>
        </div>
      </div>

      <div className="pt-4">
        <button
          onClick={onCreateAccount}
          className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700"
        >
          Create Account for this Customer
        </button>
      </div>
    </div>
  );
};
