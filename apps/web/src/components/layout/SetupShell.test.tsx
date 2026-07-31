import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import SetupShell from './SetupShell'

const logoutUser = vi.fn()
const clearToken = vi.fn()

let mockAuth = {
  isAuthenticated: true,
  userId: 'ea1bfa5b-1111-2222-3333-444455556666',
  clearToken,
}

vi.mock('@features/auth/api/auth-api', () => ({
  logoutUser: () => logoutUser(),
}))

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector(mockAuth)),
}))

beforeEach(() => {
  vi.clearAllMocks()
  logoutUser.mockResolvedValue(undefined)
  mockAuth = { isAuthenticated: true, userId: 'ea1bfa5b-1111-2222-3333-444455556666', clearToken }
})

function renderShell() {
  return render(
    <MemoryRouter initialEntries={['/welcome']}>
      <Routes>
        <Route element={<SetupShell />}>
          <Route path="/welcome" element={<div>welcome content</div>} />
        </Route>
        <Route path="/login" element={<div>login page</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('SetupShell', () => {
  it('renders the routed screen inside the shell', () => {
    renderShell()

    expect(screen.getByText('welcome content')).toBeTruthy()
    expect(screen.getByText('Nexus')).toBeTruthy()
  })

  it('lets the user log out from the welcome screen', async () => {
    const user = userEvent.setup()
    renderShell()

    await user.click(screen.getByTitle('Sair'))

    await waitFor(() => expect(screen.getByText('login page')).toBeTruthy())
    expect(clearToken).toHaveBeenCalledTimes(1)
  })
})
