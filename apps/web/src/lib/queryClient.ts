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
   React Query için tekil QueryClient oluşturur ve varsayılan ayarları merkezileştirir.
   Retry/staleTime gibi global politikalar burada yönetilir.
*/
import { QueryClient } from '@tanstack/react-query';

// Create a single QueryClient instance for the whole app
export function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        // Avoid aggressive retries by default
        retry: 1, // "1" means: 1 automatic retry after the first failure
        staleTime: 30_000, // 30s fresh window to reduce refetch churn
        refetchOnWindowFocus: false,
      },
      mutations: {
        retry: 0, // mutations usually shouldn't auto-retry unless explicitly needed
      },
    },
  });
}
