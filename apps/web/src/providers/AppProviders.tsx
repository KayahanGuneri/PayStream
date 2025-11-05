/* Türkçe Özet:
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
