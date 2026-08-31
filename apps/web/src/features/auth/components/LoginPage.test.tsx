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

  it('still greets a freshly confirmed email', () => {
    renderPage('?confirmed=true')

    expect(screen.getByText(/E-mail confirmado com sucesso/)).toBeTruthy()
  })
})
