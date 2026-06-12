import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ConfirmEmailCallbackPage from './ConfirmEmailCallbackPage'
import * as authApi from '../api/auth-api'

vi.mock('../api/auth-api')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

function renderPage(url: string) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[url]}>
        <Routes>
          <Route path="/confirm-email" element={<ConfirmEmailCallbackPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  mockNavigate.mockReset()
})

describe('ConfirmEmailCallbackPage', () => {
  describe('without token', () => {
    it('shows static pending page', () => {
      renderPage('/confirm-email')
      expect(screen.getByText(/confirme seu e-mail/i)).toBeTruthy()
      expect(screen.getByText(/o link expira em 24 horas/i)).toBeTruthy()
    })
  })

  describe('with valid token', () => {
    it('shows success and redirects after confirmation', async () => {
      vi.mocked(authApi.confirmEmail).mockResolvedValue(undefined)
      renderPage('/confirm-email?token=valid-token')

      await waitFor(() => {
        expect(screen.getByText(/e-mail confirmado/i)).toBeTruthy()
      })

      await waitFor(
        () => {
          expect(mockNavigate).toHaveBeenCalledWith('/login?confirmed=true')
        },
        { timeout: 3000 },
      )
    })
  })

  describe('with invalid token (400)', () => {
    it('shows error and resend form', async () => {
      vi.mocked(authApi.confirmEmail).mockRejectedValue({ response: { status: 400 } })
      renderPage('/confirm-email?token=bad-token')

      await waitFor(() => {
        expect(screen.getByText(/link inválido ou expirado/i)).toBeTruthy()
        expect(screen.getByRole('button', { name: /reenviar/i })).toBeTruthy()
      })
    })
  })

  describe('with already confirmed token (409)', () => {
    it('shows already confirmed message without resend form', async () => {
      vi.mocked(authApi.confirmEmail).mockRejectedValue({ response: { status: 409 } })
      renderPage('/confirm-email?token=old-token')

      await waitFor(() => {
        expect(screen.getByText(/e-mail já confirmado/i)).toBeTruthy()
        expect(screen.queryByRole('button', { name: /reenviar/i })).toBeNull()
      })
    })
  })
})
