import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import CreateOrganizationPage from './CreateOrganizationPage'

vi.mock('../api/organization-api')
vi.mock('@lib/axios', () => ({ default: { post: vi.fn() } }))
vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) =>
    selector({ setToken: vi.fn(), organizationId: null, isAuthenticated: true }),
  ),
}))

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  )
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('CreateOrganizationPage', () => {
  it('presents the screen heading, the kicker and the form', () => {
    render(<CreateOrganizationPage />, { wrapper })

    expect(screen.getByRole('heading', { level: 1, name: /criar organização/i })).toBeTruthy()
    expect(screen.getByText('Criar')).toBeTruthy()
    expect(screen.getByLabelText(/nome/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /criar organização/i })).toBeTruthy()
  })

  it('offers a way back to the welcome screen', () => {
    render(<CreateOrganizationPage />, { wrapper })

    // O Button do design system renderiza o Link como <a role="button">.
    expect(screen.getByRole('button', { name: /voltar/i }).getAttribute('href')).toBe('/welcome')
  })
})
