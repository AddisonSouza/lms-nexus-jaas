import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ResetPasswordPage from './ResetPasswordPage'

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
})
