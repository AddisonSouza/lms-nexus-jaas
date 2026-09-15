import { render, screen } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import MinimalHeader from './MinimalHeader'

const USER_ID = 'ea1bfa5b-1111-2222-3333-444455556666'

function signedOut() {
  return {
    isAuthenticated: false,
    userId: null as string | null,
    userName: null as string | null,
    userEmail: null as string | null,
    clearToken: vi.fn(),
  }
}

let mockAuth = signedOut()

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector(mockAuth)),
}))

vi.mock('@features/auth/api/auth-api', () => ({
  logoutUser: vi.fn(),
}))

beforeEach(() => {
  mockAuth = signedOut()
})

function renderHeader() {
  // O Sair usa o useLogout, que esvazia o cache do React Query (#217).
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter>
        <MinimalHeader />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('MinimalHeader', () => {
  it('offers the logout action to an authenticated user', () => {
    mockAuth = { ...signedOut(), isAuthenticated: true, userId: USER_ID, userName: 'Ana Souza' }
    renderHeader()

    expect(screen.getByTitle('Sair')).toBeTruthy()
  })

  it('shows the user name instead of the id', () => {
    mockAuth = { ...signedOut(), isAuthenticated: true, userId: USER_ID, userName: 'Ana Souza', userEmail: 'ana@test.com' }
    renderHeader()

    // O nome inteiro fica no title para quando o texto for truncado.
    expect(screen.getByText('Ana Souza').getAttribute('title')).toBe('Ana Souza')
    expect(screen.queryByText(/ea1bfa5b/)).toBeNull()
  })

  it('falls back to the e-mail when the token has no name', () => {
    mockAuth = { ...signedOut(), isAuthenticated: true, userId: USER_ID, userEmail: 'ana@test.com' }
    renderHeader()

    expect(screen.getByText('ana@test.com')).toBeTruthy()
    expect(screen.queryByText(/ea1bfa5b/)).toBeNull()
  })

  it('hides the logout action when there is no session', () => {
    renderHeader()

    // /invitations/:token/accept é rota pública: abrir deslogado não pode
    // mostrar uma saída que não existe.
    expect(screen.queryByTitle('Sair')).toBeNull()
  })

  it('always shows the product mark', () => {
    renderHeader()

    expect(screen.getByText('Nexus')).toBeTruthy()
  })
})
