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
    ],
  },
]);
