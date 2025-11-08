/* Türkçe Özet:
   Yakalanmayan yollar için 404 sayfası. Kullanıcıyı ana sayfa/Customers'a yönlendirir.
*/
import React from 'react';
import { Link, useRouteError } from 'react-router-dom';

export const NotFound: React.FC = () => {
  const err = useRouteError() as unknown;

  // Minimal, kullanıcı dostu 404
  return (
    <div className="mx-auto max-w-lg rounded-xl border bg-white p-6 text-center shadow-sm">
      <h1 className="text-2xl font-bold">404 — Not Found</h1>
      <p className="mt-2 text-gray-600">
        The page you are looking for does not exist or has moved.
      </p>
      <div className="mt-4 flex items-center justify-center gap-3">
        <Link to="/" className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">
          Go Home
        </Link>
        <Link
          to="/customers/new"
          className="rounded-md bg-gray-100 px-4 py-2 text-sm text-gray-800 hover:bg-gray-200"
        >
          Create Customer
        </Link>
      </div>

      {/* Optional: show raw error for dev */}
      {Boolean(err) && (
        <pre className="mt-4 overflow-auto rounded-md bg-gray-50 p-3 text-left text-xs text-gray-600">
{String((err as Error)?.message ?? '')}
        </pre>
      )}
    </div>
  );
};
