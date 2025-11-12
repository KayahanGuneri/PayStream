import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { router } from './routes';
import { AppProviders } from './providers/AppProviders';
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
