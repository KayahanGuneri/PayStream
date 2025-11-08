/* Türkçe Özet:
   Uygulamanın kök sağlayıcıları: React Query QueryClientProvider + sonner Toaster.
   (İsteğe bağlı) React Query Devtools kapalı başlatılır.
*/
import React, { type PropsWithChildren } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'sonner';

// Create a single QueryClient instance for the whole app
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Avoid too aggressive refetching; tune as you wish
      refetchOnWindowFocus: false,
      retry: 1, // Most of our hooks already gate retries via guards
    },
  },
});

export const AppProviders: React.FC<PropsWithChildren> = ({ children }) => {
  // Wrap the entire app with QueryClientProvider
  return (
    <QueryClientProvider client={queryClient}>
      {/* Toast system */}
      <Toaster richColors closeButton />

   Uygulama genel sağlayıcıları: React Query QueryClientProvider ve sonner Toaster.
   (İleride SocketProvider gibi başka global sağlayıcılar burada birleştirilebilir.)
*/
import React from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'sonner';
import { createQueryClient } from '../lib/queryClient';

// Keep a single client instance
const queryClient = createQueryClient();

type AppProvidersProps = {
  children: React.ReactNode;
};

export const AppProviders: React.FC<AppProvidersProps> = ({ children }) => {
  return (
    <QueryClientProvider client={queryClient}>
      {/* Global toast portal */}
      <Toaster richColors />
      {children}
    </QueryClientProvider>
  );
};
