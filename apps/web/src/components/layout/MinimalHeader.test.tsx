import { render, screen } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import MinimalHeader from './MinimalHeader'

let mockAuth = { isAuthenticated: false, userId: null as string | null, clearToken: vi.fn() }

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector(mockAuth)),
}))

vi.mock('@features/auth/api/auth-api', () => ({
  logoutUser: vi.fn(),
}))

beforeEach(() => {
  mockAuth = { isAuthenticated: false, userId: null, clearToken: vi.fn() }
})

function renderHeader() {
  return render(
    <MemoryRouter>
      <MinimalHeader />
    </MemoryRouter>,
  )
}

describe('MinimalHeader', () => {
  it('offers the logout action to an authenticated user', () => {
    mockAuth = { isAuthenticated: true, userId: 'ea1bfa5b-1111-2222-3333-444455556666', clearToken: vi.fn() }
    renderHeader()

    expect(screen.getByTitle('Sair')).toBeTruthy()
  })

  it('hides the logout action when there is no session', () => {
    mockAuth = { isAuthenticated: false, userId: null, clearToken: vi.fn() }
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
