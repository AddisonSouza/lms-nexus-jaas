import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from '@lib/query-client'
import ThemeProvider from '@components/layout/ThemeProvider'

interface ProvidersProps {
  children: React.ReactNode
}

function Providers({ children }: ProvidersProps) {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>{children}</ThemeProvider>
    </QueryClientProvider>
  )
}

export default Providers
