/* Türkçe Özet:
   Router tanımı. RootLayout altında Home ve Accounts sayfaları bulunur.
*/
import React from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { RootLayout } from './layouts/RootLayout.tsx';
import { Home } from '../pages/Home.tsx';
import { Accounts } from '../pages/Accounts.tsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <Home /> },
      { path: 'accounts', element: <Accounts /> },
    ],
  },
]);
