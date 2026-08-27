import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import CreateOrganizationForm from './CreateOrganizationForm'
import * as orgApi from '../api/organization-api'

vi.mock('../api/organization-api')
vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) =>
    selector({ setToken: vi.fn(), organizationId: null, isAuthenticated: true }),
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

  it('marks the name field invalid and announces the error', async () => {
    render(<CreateOrganizationForm />, { wrapper })
    await userEvent.click(screen.getByRole('button', { name: /criar organização/i }))

    await waitFor(() => {
      expect(screen.getByRole('alert').textContent).toMatch(/ao menos 2 caracteres/i)
    })
    expect(screen.getByLabelText(/nome/i).getAttribute('aria-invalid')).toBe('true')
  })

  it('counts the characters typed in the description', async () => {
    render(<CreateOrganizationForm />, { wrapper })
    expect(screen.getByText('0/500')).toBeTruthy()

    await userEvent.type(screen.getByLabelText(/descrição/i), 'Escola')

    await waitFor(() => expect(screen.getByText('6/500')).toBeTruthy())
  })

  it('creates org and switches organization on success', async () => {
    vi.mocked(orgApi.createOrganization).mockResolvedValue({
      id: 'org-1', name: 'Test Org', description: null, ownerId: 'u1', createdAt: '',
    })
    vi.mocked(orgApi.switchOrganization).mockResolvedValue('new-token')

    render(<CreateOrganizationForm />, { wrapper })
    await userEvent.type(screen.getByLabelText(/nome/i), 'Test Org')
    await userEvent.click(screen.getByRole('button', { name: /criar organização/i }))

    await waitFor(() => {
      expect(orgApi.createOrganization).toHaveBeenCalledWith(expect.objectContaining({ name: 'Test Org' }))
      expect(orgApi.switchOrganization).toHaveBeenCalledWith('org-1')
    })
  })

  it('shows 409 error message on duplicate name', async () => {
    vi.mocked(orgApi.createOrganization).mockRejectedValue({ response: { status: 409 } })

    render(<CreateOrganizationForm />, { wrapper })
    await userEvent.type(screen.getByLabelText(/nome/i), 'Dup Org')
    await userEvent.click(screen.getByRole('button', { name: /criar organização/i }))

    await waitFor(() => {
      expect(screen.getByRole('alert').textContent).toMatch(/já possui uma organização com esse nome/i)
    })
  })
})
