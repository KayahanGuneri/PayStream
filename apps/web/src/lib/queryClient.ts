/* Türkçe Özet:
   React Query QueryClient ayarları. Varsayılan retry/staleTime vb. burada tutulur.
   Error handling mantığı özellik (feature) seviyesinde özelleştirilebilir.
*/

import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Avoid aggressive refetches during dev
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 30_000,
    },
    mutations: {
      retry: 0,
    },
  },
});
