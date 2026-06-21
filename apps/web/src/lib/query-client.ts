import { QueryClient } from '@tanstack/react-query'
import { isAxiosError } from 'axios'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60,
      retry: (failureCount, error) => {
        // Don't retry client errors (401/403/404/…) — only transient failures.
        const status = isAxiosError(error) ? error.response?.status : undefined
        if (status && status >= 400 && status < 500) return false
        return failureCount < 1
      },
    },
  },
})
