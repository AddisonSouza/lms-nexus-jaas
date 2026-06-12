import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import CreateOrganizationForm from './CreateOrganizationForm'
import * as orgApi from '../api/organization-api'
import * as authApi from '@features/auth/api/auth-api'

vi.mock('../api/organization-api')
vi.mock('@features/auth/api/auth-api')
vi.mock('@features/auth/store/authStore', () => ({
  useAuthStore: vi.fn((selector) =>
    selector({ setOrganization: vi.fn(), organizationId: null, isAuthenticated: true }),
  ),
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

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
  mockNavigate.mockReset()
})

describe('CreateOrganizationForm', () => {
  it('renders name field and submit button', () => {
    render(<CreateOrganizationForm />, { wrapper })
    expect(screen.getByLabelText(/nome/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /criar organização/i })).toBeTruthy()
  })

  it('shows validation error for empty name', async () => {
    render(<CreateOrganizationForm />, { wrapper })
    await userEvent.click(screen.getByRole('button', { name: /criar organização/i }))
    await waitFor(() => {
      expect(screen.getByText(/ao menos 2 caracteres/i)).toBeTruthy()
    })
  })

  it('creates org and refreshes token on success', async () => {
    vi.mocked(orgApi.createOrganization).mockResolvedValue({
      id: 'org-1', name: 'Test Org', description: null, ownerId: 'u1', createdAt: '',
    })
    vi.mocked(authApi.refreshTokens).mockResolvedValue({ accessToken: 'new-token' })

    render(<CreateOrganizationForm />, { wrapper })
    await userEvent.type(screen.getByLabelText(/nome/i), 'Test Org')
    await userEvent.click(screen.getByRole('button', { name: /criar organização/i }))

    await waitFor(() => {
      expect(orgApi.createOrganization).toHaveBeenCalledWith(expect.objectContaining({ name: 'Test Org' }))
      expect(authApi.refreshTokens).toHaveBeenCalledWith('org-1')
    })
  })

  it('shows 409 error message on duplicate name', async () => {
    vi.mocked(orgApi.createOrganization).mockRejectedValue({ response: { status: 409 } })

    render(<CreateOrganizationForm />, { wrapper })
    await userEvent.type(screen.getByLabelText(/nome/i), 'Dup Org')
    await userEvent.click(screen.getByRole('button', { name: /criar organização/i }))

    await waitFor(() => {
      expect(screen.getByText(/já possui uma organização com esse nome/i)).toBeTruthy()
    })
  })
})
