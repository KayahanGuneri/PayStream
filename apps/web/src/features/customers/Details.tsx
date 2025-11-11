/* Türkçe Özet:
   Tekil müşteri görüntüleme bileşeni (UI-only). CustomerDTO alır, bilgileri listeler
   ve dışarıdan verilen "hesap oluştur" aksiyonunu butonla tetikler.
*/

import React from 'react';
import type { CustomerDTO } from '../../hooks/useCustomers';

export type CustomerDetailsProps = {
  customer: CustomerDTO;
  onCreateAccount?: () => void; // Trigger navigation to /accounts?customerId=...
};

export const CustomerDetails: React.FC<CustomerDetailsProps> = ({ customer, onCreateAccount }) => {
  // Render a simple card with customer information
  return (
    <div className="rounded-xl border bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold">Customer Details</h2>
      <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <div>
          <dt className="text-xs text-gray-500">ID</dt>
          <dd className="break-all text-sm">{customer.id}</dd>
        </div>
        <div>
          <dt className="text-xs text-gray-500">Name</dt>
          <dd className="text-sm">{customer.name}</dd>
        </div>
        <div>
          <dt className="text-xs text-gray-500">Email</dt>
          <dd className="text-sm">{customer.email}</dd>
        </div>
      </dl>

      <div className="mt-4">
        <button
          type="button"
          onClick={onCreateAccount}
          className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700"
        >
          Create account for this customer
        </button>
      </div>
    </div>
  );
};
