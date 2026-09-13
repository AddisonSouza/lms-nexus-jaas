import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useLogout } from './useLogout'

const logoutUser = vi.fn()
const signOut = vi.fn()
const clearToken = vi.fn()

vi.mock('../api/auth-api', () => ({
  logoutUser: () => logoutUser(),
}))

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ signOut, clearToken })),
}))

beforeEach(() => {
  vi.clearAllMocks()
  logoutUser.mockResolvedValue(undefined)
})

function LogoutButton() {
  const logout = useLogout()
  return <button onClick={logout}>Sair</button>
}

function renderHook() {
  return render(
    <MemoryRouter initialEntries={['/welcome']}>
      <Routes>
        <Route path="/welcome" element={<LogoutButton />} />
        <Route path="/login" element={<div>login page</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('useLogout', () => {
  it('ends the session on the server, signs out and goes to /login', async () => {
    const user = userEvent.setup()
    renderHook()

    await user.click(screen.getByRole('button', { name: 'Sair' }))

    await waitFor(() => expect(screen.getByText('login page')).toBeTruthy())
    expect(logoutUser).toHaveBeenCalledTimes(1)
    // signOut, e não clearToken: a saída foi escolhida, e a tela de aceite de
    // convite precisa saber disso para não levar o convite ao login (#210).
    expect(signOut).toHaveBeenCalledTimes(1)
    expect(clearToken).not.toHaveBeenCalled()
  })

  it('still clears the session locally when the server call fails', async () => {
    logoutUser.mockRejectedValue(new Error('network down'))
    const user = userEvent.setup()
    renderHook()

    await user.click(screen.getByRole('button', { name: 'Sair' }))

    await waitFor(() => expect(screen.getByText('login page')).toBeTruthy())
    expect(signOut).toHaveBeenCalledTimes(1)
  })
})
