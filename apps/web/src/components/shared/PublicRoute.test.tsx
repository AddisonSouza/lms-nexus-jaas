import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Routes, Route, useParams } from 'react-router-dom'
import PublicRoute from './PublicRoute'

let mockAuth = { isAuthenticated: false, isBootstrapping: false }

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector: (s: typeof mockAuth) => unknown) => selector(mockAuth)),
}))

function AcceptScreen() {
  const { token } = useParams<{ token: string }>()
  return <p>aceite {token}</p>
}

function renderAt(entry: string) {
  return render(
    <MemoryRouter initialEntries={[entry]}>
      <Routes>
        <Route
          path="/login"
          element={
            <PublicRoute>
              <p>tela de login</p>
            </PublicRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicRoute>
              <p>tela de cadastro</p>
            </PublicRoute>
          }
        />
        <Route path="/invitations/:token/accept" element={<AcceptScreen />} />
        <Route path="/" element={<p>raiz</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

beforeEach(() => {
  mockAuth = { isAuthenticated: false, isBootstrapping: false }
})

describe('PublicRoute', () => {
  it('shows the public page to a visitor without a session', () => {
    renderAt('/login?invite=tok123')

    expect(screen.getByText('tela de login')).toBeTruthy()
  })

  it('sends a signed-in user to the app root when no invitation is in the URL', () => {
    mockAuth = { isAuthenticated: true, isBootstrapping: false }
    renderAt('/login')

    expect(screen.getByText('raiz')).toBeTruthy()
  })

  // A causa do #198: este redirect para / passava por cima do useLogin, e em /
  // só um convite ainda pendente levava ao aceite.
  it('sends a signed-in user to the invitation that brought them to login', () => {
    mockAuth = { isAuthenticated: true, isBootstrapping: false }
    renderAt('/login?invite=tok123')

    expect(screen.getByText('aceite tok123')).toBeTruthy()
    expect(screen.queryByText('raiz')).toBeNull()
  })

  it('applies the same rule on registration', () => {
    mockAuth = { isAuthenticated: true, isBootstrapping: false }
    renderAt('/register?invite=tok123')

    expect(screen.getByText('aceite tok123')).toBeTruthy()
  })

  it('treats an empty invite parameter as no invitation', () => {
    mockAuth = { isAuthenticated: true, isBootstrapping: false }
    renderAt('/login?invite=')

    expect(screen.getByText('raiz')).toBeTruthy()
  })

  it('waits for the session to be restored before deciding', () => {
    mockAuth = { isAuthenticated: false, isBootstrapping: true }
    renderAt('/login?invite=tok123')

    expect(screen.queryByText('tela de login')).toBeNull()
    expect(screen.queryByText('aceite tok123')).toBeNull()
    expect(screen.queryByText('raiz')).toBeNull()
  })
})
