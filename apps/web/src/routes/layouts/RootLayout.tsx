import React from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';

export const RootLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
          <Link to="/" className="flex items-center gap-2 font-semibold">
            <img src="/brand/logo-primary.png" alt="PayStream" className="h-5 w-5" />
            <span>PayStream</span>
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            <NavLink to="/" end className={({isActive}) => isActive ? 'text-blue-600' : 'text-gray-600 hover:text-gray-900'}>
              Home
            </NavLink>
            <NavLink to="/accounts" className={({isActive}) => isActive ? 'text-blue-600' : 'text-gray-600 hover:text-gray-900'}>
              Accounts
            </NavLink>
            <NavLink to="/customers/new" className={({isActive}) => isActive ? 'text-blue-600' : 'text-gray-600 hover:text-gray-900'}>
              Customers
            </NavLink>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8">
        <Outlet />
      </main>

      <footer className="border-t bg-white">
        <div className="mx-auto max-w-7xl px-4 py-6 text-xs text-gray-500">
          © {new Date().getFullYear()} PayStream
        </div>
      </footer>
    </div>
  );
};
