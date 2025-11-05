/* Türkçe Özet:
   Yeni hesap oluşturma formu; UI-only. Kontrollü form kullanır ve submit'i
   dışarıdan gelen onSubmit callback’ine devreder. 422 alan hatalarını props’tan gösterir.
*/

import React, { useState } from 'react';
import type { CreateAccountFormValues } from '../../types/accounts';

export type CreateFormProps = {
  initialCustomerId?: string;
  pending?: boolean;
  onSubmit: (values: CreateAccountFormValues) => void | Promise<void>;
  fieldErrors?: Record<string, string>;
  generalErrorMessage?: string;
};

export const CreateForm: React.FC<CreateFormProps> = ({
  initialCustomerId,
  pending,
  onSubmit,
  fieldErrors,
  generalErrorMessage,
}) => {
  const [customerId, setCustomerId] = useState<string>(initialCustomerId ?? '');
  const [currency, setCurrency] = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({ customerId, currency });
  };

  const fieldMsg = (name: string) => fieldErrors?.[name];

  return (
    <div className="rounded-xl border bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold">Create Account</h2>

      {generalErrorMessage && (
        <div className="mb-3 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
          {generalErrorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Customer ID */}
        <div>
          <label className="block text-sm font-medium text-gray-700" htmlFor="customerId">
            Customer ID
          </label>
          <input
            id="customerId"
            type="text"
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
            placeholder="00000000-0000-0000-0000-000000000000"
            className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
              fieldMsg('customerId') ? 'border-red-300' : 'border-gray-300'
            }`}
          />
          {fieldMsg('customerId') && (
            <p className="mt-1 text-xs text-red-700">{fieldMsg('customerId')}</p>
          )}
        </div>

        {/* Currency */}
        <div>
          <label className="block text-sm font-medium text-gray-700" htmlFor="currency">
            Currency (3-letter uppercase)
          </label>
          <input
            id="currency"
            type="text"
            value={currency}
            onChange={(e) => setCurrency(e.target.value)}
            placeholder="TRY"
            className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
              fieldMsg('currency') ? 'border-red-300' : 'border-gray-300'
            }`}
          />
          {fieldMsg('currency') && (
            <p className="mt-1 text-xs text-red-700">{fieldMsg('currency')}</p>
          )}
        </div>

        <div className="pt-2">
          <button
            type="submit"
            disabled={pending}
            className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 disabled:opacity-50"
          >
            {pending ? 'Creating…' : 'Create Account'}
          </button>
        </div>
      </form>
    </div>
  );
};
