import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import LoginForm from './LoginForm'
import * as authApi from '../api/auth-api'

vi.mock('../api/auth-api')
vi.mock('../store/authStore', () => ({
  useAuthStore: vi.fn((selector) =>
    selector({ accessToken: null, isAuthenticated: false, setToken: vi.fn(), clearToken: vi.fn() }),
  ),
}))

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={new QueryClient({ defaultOptions: { mutations: { retry: false } } })}>
    <MemoryRouter>{children}</MemoryRouter>
  </QueryClientProvider>
)

beforeEach(() => vi.clearAllMocks())

describe('LoginForm', () => {
  it('renders email and password fields', () => {
    render(<LoginForm />, { wrapper })
    expect(screen.getByLabelText(/e-mail/i)).toBeTruthy()
    expect(screen.getByLabelText(/senha/i)).toBeTruthy()
  })

  it('shows validation errors for empty submit', async () => {
    render(<LoginForm />, { wrapper })
    await userEvent.click(screen.getByRole('button', { name: /entrar/i }))
    await waitFor(() => {
      expect(screen.getByText(/e-mail é obrigatório/i)).toBeTruthy()
    })
  })

  it('shows 401 error message on invalid credentials', async () => {
    vi.mocked(authApi.loginUser).mockRejectedValue({ response: { status: 401 } })
    render(<LoginForm />, { wrapper })
    await userEvent.type(screen.getByLabelText(/e-mail/i), 'user@test.com')
    await userEvent.type(screen.getByLabelText(/senha/i), 'wrongpass')
    await userEvent.click(screen.getByRole('button', { name: /entrar/i }))
    await waitFor(() => {
      expect(screen.getByText(/e-mail ou senha inválidos/i)).toBeTruthy()
    })
  })
})
