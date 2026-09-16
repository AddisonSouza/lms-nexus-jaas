import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ResetPasswordPage from './ResetPasswordPage'
import * as authApi from '../api/auth-api'

vi.mock('../api/auth-api')

function renderPage(url: string) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[url]}>
        <Routes>
          <Route path="/reset-password" element={<ResetPasswordPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => vi.clearAllMocks())

describe('ResetPasswordPage', () => {
  it('keeps a way back to login when the token is missing', () => {
    renderPage('/reset-password')

    expect(screen.getByText('Link de redefinição inválido.')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Voltar ao login' }).getAttribute('href')).toBe('/login')
  })

  it('shows the form with a way back to login when the token is present', () => {
    renderPage('/reset-password?token=abc')

    expect(screen.getByLabelText('Nova senha')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Voltar ao login' })).toBeTruthy()
  })

  // O schema desta tela só exige 8 caracteres; a política forte vive no
  // back-end (#264), então é o 422 que diz o que faltou.
  it('lists the criteria the password missed instead of a generic failure', async () => {
    vi.mocked(authApi.resetPassword).mockRejectedValue({
      response: {
        status: 422,
        data: { errors: ['resetPassword.arg0.newPassword: A senha precisa de: letra maiúscula, símbolo'] },
      },
    })

    renderPage('/reset-password?token=abc')
    await userEvent.type(screen.getByLabelText('Nova senha'), 'senhafraca')
    await userEvent.type(screen.getByLabelText('Confirmar nova senha'), 'senhafraca')
    await userEvent.click(screen.getByRole('button', { name: /redefinir senha/i }))

    const alert = await screen.findByRole('alert')
    expect(alert.textContent).toBe('A senha precisa de: letra maiúscula, símbolo')
  })

  it('tells the user to ask for a new link when the token is spent', async () => {
    vi.mocked(authApi.resetPassword).mockRejectedValue({
      response: { status: 400, data: { error: 'Token inválido, expirado ou já utilizado' } },
    })

    renderPage('/reset-password?token=abc')
    await userEvent.type(screen.getByLabelText('Nova senha'), 'Senha@123')
    await userEvent.type(screen.getByLabelText('Confirmar nova senha'), 'Senha@123')
    await userEvent.click(screen.getByRole('button', { name: /redefinir senha/i }))

    const alert = await screen.findByRole('alert')
    expect(alert.textContent).toBe('O link de redefinição é inválido ou já expirou. Solicite um novo.')
  })
})
