import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import LoginPage from './LoginPage'

const loginUser = vi.fn()
const setToken = vi.fn()
const mockNavigate = vi.fn()

vi.mock('../api/auth-api', () => ({
  loginUser: (data: unknown) => loginUser(data),
}))

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ setToken })),
}))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

function renderPage(search = '') {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[`/login${search}`]}>
        <LoginPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

async function submitLogin() {
  await userEvent.type(screen.getByLabelText(/e-mail/i), 'user@test.com')
  await userEvent.type(screen.getByLabelText('Senha'), 'password123')
  await userEvent.click(screen.getByRole('button', { name: /entrar/i }))
}

beforeEach(() => {
  vi.clearAllMocks()
  loginUser.mockResolvedValue({ accessToken: 'jwt-token' })
})

describe('LoginPage', () => {
  it('returns to the invitation after signing in when one brought the user here', async () => {
    renderPage('?invite=tok123')

    await submitLogin()

    await waitFor(() =>
      expect(mockNavigate).toHaveBeenCalledWith('/invitations/tok123/accept'),
    )
  })

  it('goes to the app root when there is no invitation', async () => {
    renderPage()

    await submitLogin()

    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/'))
  })

  it('carries the invitation to the register link, so a newcomer does not lose it', () => {
    renderPage('?invite=tok123')

    const register = screen.getByRole('link', { name: 'Criar conta' })
    expect(register.getAttribute('href')).toBe('/register?invite=tok123')
  })

  it('links to plain registration when there is no invitation', () => {
    renderPage()

    expect(screen.getByRole('link', { name: 'Criar conta' }).getAttribute('href')).toBe('/register')
  })

  it('shows how long the block lasts instead of blaming the password', async () => {
    loginUser.mockRejectedValue({
      response: { status: 429, headers: { 'retry-after': '900' }, data: { error: 'AUTH_RATE_LIMIT_EXCEEDED' } },
    })
    renderPage()

    await submitLogin()

    await waitFor(() =>
      expect(screen.getByText('Muitas tentativas. Tente novamente em 15 minutos.')).toBeTruthy(),
    )
    expect(screen.queryByText('E-mail ou senha inválidos.')).toBeNull()
  })

  it('keeps the submit button closed while the block lasts', async () => {
    loginUser.mockRejectedValue({
      response: { status: 429, headers: { 'retry-after': '900' }, data: { error: 'AUTH_RATE_LIMIT_EXCEEDED' } },
    })
    renderPage()

    await submitLogin()

    await waitFor(() =>
      expect(screen.getByRole('button', { name: /entrar/i }).hasAttribute('disabled')).toBe(true),
    )
  })

  it('still names a wrong password for a plain 401', async () => {
    loginUser.mockRejectedValue({ response: { status: 401, headers: {}, data: {} } })
    renderPage()

    await submitLogin()

    await waitFor(() => expect(screen.getByText('E-mail ou senha inválidos.')).toBeTruthy())
    expect(screen.getByRole('button', { name: /entrar/i }).hasAttribute('disabled')).toBe(false)
  })

  it('still greets a freshly confirmed email', () => {
    renderPage('?confirmed=true')

    expect(screen.getByText(/E-mail confirmado com sucesso/)).toBeTruthy()
  })
})
