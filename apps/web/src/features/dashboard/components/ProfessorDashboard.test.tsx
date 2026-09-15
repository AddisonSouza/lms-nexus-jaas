import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AxiosError, AxiosHeaders } from 'axios'
import ProfessorDashboard from './ProfessorDashboard'
import * as dashboardApi from '../api/professor-dashboard'

vi.mock('../api/professor-dashboard')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>
}

function httpError(status: number) {
  const headers = new AxiosHeaders()
  return new AxiosError('error', String(status), { headers }, null, {
    status,
    statusText: '',
    headers,
    config: { headers },
    data: null,
  })
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('ProfessorDashboard', () => {
  it('renders nothing when the user does not teach the subject (403)', async () => {
    vi.mocked(dashboardApi.getProfessorDashboard).mockRejectedValue(httpError(403))
    const { container } = render(<ProfessorDashboard subjectId="subject-1" />, { wrapper })

    await waitFor(() => {
      expect(screen.queryByText(/carregando dashboard/i)).toBeNull()
    })
    expect(container.innerHTML).toBe('')
  })

  it('shows the error message for other failures', async () => {
    vi.mocked(dashboardApi.getProfessorDashboard).mockRejectedValue(httpError(500))
    render(<ProfessorDashboard subjectId="subject-1" />, { wrapper })

    await waitFor(() => {
      expect(screen.getByText(/não foi possível carregar o dashboard/i)).toBeTruthy()
    })
  })
})
