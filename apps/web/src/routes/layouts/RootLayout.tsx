/* Türkçe Özet:
   Uygulamanın temel layout bileşeni. Header, navigation bar ve Outlet içerir.
*/

import React from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';

export const RootLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="sticky top-0 z-10 border-b bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <img
              src="/brand/logo-primary.png"
              alt="PayStream"
              className="h-8 w-auto"
            />
            <span className="font-semibold text-blue-600 text-lg">
              PayStream
            </span>
          </Link>

          {/* Navigation */}
          <nav className="flex items-center gap-4 text-sm">
            <NavLink
              to="/"
              className={({ isActive }) =>
                `px-2 py-1 ${
                  isActive
                    ? 'font-semibold text-blue-600'
                    : 'text-gray-700 hover:text-gray-900'
                }`
              }
            >
              Home
            </NavLink>
            <NavLink
              to="/accounts"
              className={({ isActive }) =>
                `px-2 py-1 ${
                  isActive
                    ? 'font-semibold text-blue-600'
                    : 'text-gray-700 hover:text-gray-900'
                }`
              }
            >
              Accounts
            </NavLink>
          </nav>
        </div>
      </header>

      {/* İçerik */}
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
};
