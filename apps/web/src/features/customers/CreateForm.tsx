/* Türkçe Özet:
   Customer oluşturma formu (UI-only). Kontrollü formdur; submit işlemini
   dışarıdan verilen onSubmit callback'ine devreder. 422 ValidationError
   alan mesajlarını input altında gösterir, genel hatayı üstte yüzeye çıkarır.
*/

import React, { useState } from 'react';

export type CreateCustomerValues = {
  name: string;
  email: string;
  password: string;
};

export type CreateCustomerFormProps = {
  pending?: boolean;
  onSubmit: (values: CreateCustomerValues) => void | Promise<void>;
  fieldErrors?: Record<string, string>;
  generalErrorMessage?: string;
};

export const CreateCustomerForm: React.FC<CreateCustomerFormProps> = ({
  pending,
  onSubmit,
  fieldErrors,
  generalErrorMessage,
}) => {
  // Controlled fields
  const [name, setName] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');

  // Handle native form submit
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({ name, email, password });
  };

  // Helper to pick a field error if exists
  const f = (key: string) => fieldErrors?.[key];

  return (
    <div className="rounded-xl border bg-white p-6 shadow-sm">
      <h2 className="mb-4 text-lg font-semibold">Create Customer</h2>

      {generalErrorMessage && (
        <div className="mb-3 rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
          {generalErrorMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Name */}
        <div>
          <label htmlFor="c-name" className="block text-sm font-medium text-gray-700">
            Name
          </label>
          <input
            id="c-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Jane Doe"
            maxLength={128}                 // aligns with BE @Size(max=128)
            autoComplete="name"
            className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
              f('name') ? 'border-red-300' : 'border-gray-300'
            }`}
          />
          {f('name') && <p className="mt-1 text-xs text-red-700">{f('name')}</p>}
        </div>

        {/* Email */}
        <div>
          <label htmlFor="c-email" className="block text-sm font-medium text-gray-700">
            Email
          </label>
          <input
            id="c-email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="jane@example.com"
            maxLength={320}                 // aligns with BE @Size(max=320)
            autoComplete="email"
            className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
              f('email') ? 'border-red-300' : 'border-gray-300'
            }`}
          />
          {f('email') && <p className="mt-1 text-xs text-red-700">{f('email')}</p>}
        </div>

        {/* Password */}
        <div>
          <label htmlFor="c-password" className="block text-sm font-medium text-gray-700">
            Password
          </label>
          <input
            id="c-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            minLength={8}                   // aligns with BE @Size(min=8, max=72)
            maxLength={72}
            autoComplete="new-password"
            className={`mt-1 w-full rounded-md border px-3 py-2 text-sm outline-none ${
              f('password') ? 'border-red-300' : 'border-gray-300'
            }`}
          />
          {f('password') && <p className="mt-1 text-xs text-red-700">{f('password')}</p>}
        </div>

        {/* Submit */}
        <div className="pt-2">
          <button
            type="submit"
            disabled={pending}
            className="inline-flex items-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 disabled:opacity-50"
          >
            {pending ? 'Creating…' : 'Create Customer'}
          </button>
        </div>
      </form>
    </div>
  );
};
