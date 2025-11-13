// Türkçe Özet:
// Router temel duman testi: /accounts yoluna gidildiğinde sayfa render olmalı.

import React from 'react';
import { describe, it, expect } from 'vitest';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { screen, render } from '@testing-library/react';

// adjust these imports to your project structure
import { RootLayout } from '../layouts/RootLayout';
import { Accounts } from '../../pages/Accounts';

describe('Router smoke /accounts', () => {
  it('renders Accounts page', async () => {
    const router = createMemoryRouter(
      [
        {
          path: '/',
          element: <RootLayout />,
          children: [
            { path: 'accounts', element: <Accounts /> },
          ],
        },
      ],
      { initialEntries: ['/accounts'] },
    );

    render(<RouterProvider router={router} />);
    expect(await screen.findByText(/Accounts/i)).toBeInTheDocument();
  });
});
