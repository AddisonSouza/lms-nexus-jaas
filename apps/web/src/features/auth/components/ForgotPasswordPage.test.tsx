import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import ForgotPasswordPage from './ForgotPasswordPage'
import * as authApi from '../api/auth-api'

vi.mock('../api/auth-api')

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={new QueryClient({ defaultOptions: { mutations: { retry: false } } })}>
    <MemoryRouter>{children}</MemoryRouter>
  </QueryClientProvider>
)

beforeEach(() => vi.clearAllMocks())

describe('ForgotPasswordPage', () => {
  it('renders email field and submit button', () => {
    render(<ForgotPasswordPage />, { wrapper })
    expect(screen.getByLabelText(/e-mail/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /enviar link/i })).toBeTruthy()
  })

  it('calls forgotPassword and shows success message on submit', async () => {
    vi.mocked(authApi.forgotPassword).mockResolvedValue(undefined)
    render(<ForgotPasswordPage />, { wrapper })

    await userEvent.type(screen.getByLabelText(/e-mail/i), 'user@test.com')
    await userEvent.click(screen.getByRole('button', { name: /enviar link/i }))

    await waitFor(() => {
      expect(authApi.forgotPassword).toHaveBeenCalledWith('user@test.com')
      expect(screen.getByText(/e-mail enviado/i)).toBeTruthy()
    })
  })

  it('keeps a way back to login in the success state', async () => {
    vi.mocked(authApi.forgotPassword).mockResolvedValue(undefined)
    render(<ForgotPasswordPage />, { wrapper })

    await userEvent.type(screen.getByLabelText(/e-mail/i), 'user@test.com')
    await userEvent.click(screen.getByRole('button', { name: /enviar link/i }))

    await waitFor(() => {
      expect(screen.getByRole('link', { name: 'Voltar ao login' }).getAttribute('href')).toBe('/login')
    })
  })

  it('explains the block instead of failing silently', async () => {
    vi.mocked(authApi.forgotPassword).mockRejectedValue({
      response: { status: 429, headers: { 'retry-after': '900' }, data: { error: 'AUTH_RATE_LIMIT_EXCEEDED' } },
    })
    render(<ForgotPasswordPage />, { wrapper })

    await userEvent.type(screen.getByLabelText(/e-mail/i), 'user@test.com')
    await userEvent.click(screen.getByRole('button', { name: /enviar link/i }))

    await waitFor(() =>
      expect(screen.getByRole('alert').textContent).toBe(
        'Muitas tentativas. Tente novamente em 15 minutos.',
      ),
    )
  })
})
