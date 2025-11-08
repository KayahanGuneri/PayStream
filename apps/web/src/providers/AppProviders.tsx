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
      {children}
    </QueryClientProvider>
  );
};
