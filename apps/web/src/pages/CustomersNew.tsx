import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { CreateCustomerForm } from './Customers';
import type { CreateCustomerValues } from './Customers';
import { useCreateCustomer, extractFieldErrors } from '../hooks/useCustomers';
import { AuthError, ValidationError, ApiError } from '../lib/errors';

export const CustomersNewPage: React.FC = () => {
  const navigate = useNavigate();
  const createMutation = useCreateCustomer();

  // --- NEW: “ID ile git” barı state’i
  const [gotoId, setGotoId] = useState('');

  const fieldErrors = useMemo(
    () => extractFieldErrors(createMutation.error),
    [createMutation.error]
  );

  const handleGoto = (e: React.FormEvent) => {
    e.preventDefault();
    const id = gotoId.trim();
    if (!id) return;
    navigate(`/customers/${encodeURIComponent(id)}`);
  };

  const handleCreate = async (values: CreateCustomerValues): Promise<void> => {
    try {
      const payload = {
        name: values.name.trim(),
        email: values.email.trim().toLowerCase(),
        password: values.password,
      };
      const created = await createMutation.mutateAsync(payload);
      toast.success('Customer created successfully.');
      navigate(`/customers/${encodeURIComponent(created.id)}`);
    } catch (err: unknown) {
      if (err instanceof AuthError) {
        toast.error('Authentication/authorization required. (Redirect TODO)');
        return;
      }
      if (err instanceof ValidationError) {
        toast.warning((err as Error).message || 'Validation failed.');
        return;
      }
      if (err instanceof ApiError) {
        toast.error((err as Error).message || 'Request failed.');
        return;
      }
      toast.error((err as Error)?.message ?? 'Unexpected error.');
    }
  };

  return (
    <div className="space-y-8">
      {/* Hero */}
      <div className="overflow-hidden rounded-2xl border bg-white shadow-sm">
        <img src="/brand/ai-assist.png" alt="Customers Banner" className="h-40 w-full object-cover sm:h-56" />
        <div className="p-6">
          <h1 className="text-2xl font-bold">Create / Find Customer</h1>
          <p className="mt-2 text-gray-600">Create a new customer or open an existing one by ID.</p>
        </div>
      </div>

      {/* NEW: ID ile git barı */}
      <form onSubmit={handleGoto} className="rounded-xl border bg-white p-4 shadow-sm flex gap-3 items-end">
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-700">Open by Customer ID (UUID)</label>
          <input
            type="text"
            value={gotoId}
            onChange={(e) => setGotoId(e.target.value)}
            placeholder="00000000-0000-0000-0000-000000000000"
            className="mt-1 w-full rounded-md border px-3 py-2 text-sm"
          />
        </div>
        <button
          type="submit"
          className="h-10 rounded-md bg-blue-600 px-4 text-sm font-medium text-white hover:bg-blue-700"
        >
          Open
        </button>
      </form>

      {/* Create form (değişmedi) */}
      <CreateCustomerForm
        pending={createMutation.isPending}
        onSubmit={handleCreate}
        fieldErrors={fieldErrors}
        generalErrorMessage={
          createMutation.error instanceof ValidationError
            ? undefined
            : createMutation.error
            ? (createMutation.error as Error).message ?? 'Request failed.'
            : undefined
        }
      />
    </div>
  );
};
