/* Türkçe Özet:
   Uygulama yönlendirmeleri. RootLayout altında Home, Accounts ve Customers sayfaları.
   errorElement olarak ayrı dosyadaki RouteError kullanılır.
*/
import React from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { RootLayout } from './layouts/RootLayout';
import { Home } from '../pages/Home';
import { Accounts } from '../pages/Accounts';
import { CustomersNewPage } from '../pages/CustomersNew';
import { CustomerDetailsPage } from '../pages/CustomerDetailsPage';
import { RouteError } from './RouteError'; // moved to separate file to fix react-refresh rule

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
    errorElement: <RouteError />,
    children: [
      { index: true, element: <Home /> },
      { path: 'accounts', element: <Accounts /> },

      // Customers
      { path: 'customers/new', element: <CustomersNewPage /> },
      { path: 'customers/:id', element: <CustomerDetailsPage /> },

    children: [
      { index: true, element: <Home /> },
      { path: 'accounts', element: <Accounts /> },
    ],
  },
]);
