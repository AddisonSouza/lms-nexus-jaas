import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import AcceptInvitePage from './AcceptInvitePage'
import * as invitationApi from '../api/invitation-api'

vi.mock('../api/invitation-api')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

let mockIsAuthenticated = true

vi.mock('@features/auth/store/authStore', () => ({
  useAuthStore: vi.fn((selector: (s: { isAuthenticated: boolean }) => unknown) =>
    selector({ isAuthenticated: mockIsAuthenticated }),
  ),
}))

const mockInfo: invitationApi.InvitationInfo = {
  organizationId: 'org-1',
  organizationName: 'Escola Teste',
  email: 'user@test.com',
  role: 'PROFESSOR',
  expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
}

function renderPage(token = 'abc123') {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[`/invitations/${token}/accept`]}>
        <Routes>
          <Route path="/invitations/:token/accept" element={<AcceptInvitePage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  mockNavigate.mockReset()
  mockIsAuthenticated = true
})

describe('AcceptInvitePage', () => {
  describe('when authenticated', () => {
    it('shows org name, role, and accept button after loading', async () => {
      vi.mocked(invitationApi.getInvitationInfo).mockResolvedValue(mockInfo)

      renderPage()

      await waitFor(() => {
        expect(screen.getByText(/escola teste/i)).toBeTruthy()
        expect(screen.getByText(/professor/i)).toBeTruthy()
        expect(screen.getByRole('button', { name: /aceitar convite/i })).toBeTruthy()
      })
    })

    it('calls acceptInvitation and navigates to org on button click', async () => {
      vi.mocked(invitationApi.getInvitationInfo).mockResolvedValue(mockInfo)
      vi.mocked(invitationApi.acceptInvitation).mockResolvedValue(undefined)

      renderPage('abc123')

      await waitFor(() => screen.getByRole('button', { name: /aceitar convite/i }))
      await userEvent.click(screen.getByRole('button', { name: /aceitar convite/i }))

      await waitFor(() => {
        expect(invitationApi.acceptInvitation).toHaveBeenCalledWith('abc123')
        expect(mockNavigate).toHaveBeenCalledWith('/organizations/org-1')
      })
    })

    it('shows error message when invitation is expired (410)', async () => {
      vi.mocked(invitationApi.getInvitationInfo).mockResolvedValue(mockInfo)
      vi.mocked(invitationApi.acceptInvitation).mockRejectedValue({ response: { status: 410 } })

      renderPage()

      await waitFor(() => screen.getByRole('button', { name: /aceitar convite/i }))
      await userEvent.click(screen.getByRole('button', { name: /aceitar convite/i }))

      await waitFor(() => {
        expect(screen.getByText(/expirou/i)).toBeTruthy()
      })
    })

    it('shows error when invitation not found (404)', async () => {
      vi.mocked(invitationApi.getInvitationInfo).mockRejectedValue({ response: { status: 404 } })

      renderPage()

      await waitFor(() => {
        expect(screen.getByText(/inválido ou expirado/i)).toBeTruthy()
      })
    })
  })

  describe('when unauthenticated', () => {
    it('redirects to /register?invite=TOKEN', () => {
      mockIsAuthenticated = false

      renderPage('tok123')

      expect(mockNavigate).toHaveBeenCalledWith('/register?invite=tok123', { replace: true })
    })
  })
})
