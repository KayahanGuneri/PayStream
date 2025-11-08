/* Türkçe Özet:
   RouterProvider, AppProviders içinde köke render edilir. Tailwind stilleri index.css'ten gelir.
   RouterProvider bileşenini AppProviders (QueryClientProvider + Toaster) ile sarar.
   Böylece tüm sayfalarda React Query ve toast kullanılabilir.
*/
import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { router } from './routes';
import { AppProviders } from './providers/AppProviders';
import './index.css';
import { router } from './routes/index.tsx';
import { AppProviders } from './providers/AppProviders.tsx';
import './index.css';

const rootEl = document.getElementById('root');
if (!rootEl) throw new Error('#root element not found in index.html');

ReactDOM.createRoot(rootEl).render(
  <React.StrictMode>
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  </React.StrictMode>
);

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

const rootEl = document.getElementById('root');
if (!rootEl) {
  throw new Error('#root element not found in index.html');
}

ReactDOM.createRoot(rootEl).render(
  <React.StrictMode>
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  </React.StrictMode>
);
