import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from '@lib/query-client'
import ThemeProvider from '@components/layout/ThemeProvider'

interface ProvidersProps {
  children: React.ReactNode
}

// No ThemeProvider: the app doesn't offer light/dark switching yet, only the
// static :root tokens in index.css. Auth/org state lives in useAuthStore (@store)
// rather than a React Context.
function Providers({ children }: ProvidersProps) {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>{children}</ThemeProvider>
    </QueryClientProvider>
  )
}

export default Providers
