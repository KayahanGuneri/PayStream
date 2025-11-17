// src/routes/__tests__/router-smoke.test.tsx
// Türkçe Özet:
// Router için temel duman testi. /accounts rotasına gidildiğinde
// Accounts sayfasının AppProviders (React Query + Toaster) ile birlikte
// sorunsuz render olduğunu doğrular.

import React from 'react';
import { describe, it, expect } from 'vitest';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { render, screen } from '@testing-library/react';

import { RootLayout } from '../layouts/RootLayout';
import { Accounts } from '../../pages/Accounts';
import { AppProviders } from '../../providers/AppProviders';

describe('Router smoke /accounts', () => {
  it('renders Accounts page', async () => {
    const router = createMemoryRouter(
      [
        {
          path: '/',
          element: <RootLayout />,
          children: [{ path: 'accounts', element: <Accounts /> }],
        },
      ],
      { initialEntries: ['/accounts'] }
    );

    // Wrap router with AppProviders so React Query etc. are available
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>
    );

    // Navbar link'ini değil, sayfa başlığını (h1) hedefleyelim
    const heading = await screen.findByRole('heading', { name: /Accounts/i });
    expect(heading).toBeInTheDocument();
  });
});
