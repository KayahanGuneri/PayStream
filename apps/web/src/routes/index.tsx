/* Türkçe Özet:
   RootLayout altında Home, Accounts, Customers rotaları.
*/
import React from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { RootLayout } from './layouts/RootLayout';
import { Home } from '../pages/Home';
import { Accounts } from '../pages/Accounts';
import { CustomersNewPage } from '../pages/CustomersNew';
import { CustomerDetailsPage } from '../pages/CustomerDetailsPage';
import { RouteError } from './RouteError';

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
      { path: 'customers/:id', element: <CustomerDetailsPage /> }
    ]
  }
]);
