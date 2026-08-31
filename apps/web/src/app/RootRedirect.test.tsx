import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import RootRedirect from './RootRedirect'
import * as invitationApi from '@features/invitation/api/invitation-api'

vi.mock('@features/invitation/api/invitation-api')

let organizationId: string | null = null
vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ organizationId })),
}))

function renderRedirect() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/classrooms" element={<p>turmas</p>} />
          <Route path="/welcome" element={<p>boas-vindas</p>} />
          <Route path="/invitations/:token/accept" element={<p>aceite</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const invitation = {
  token: 'tok-1',
  organizationId: 'org-1',
  organizationName: 'Escola Alfa',
  role: 'PROFESSOR' as const,
  expiresAt: '2026-09-06T10:00:00Z',
}

beforeEach(() => {
  vi.clearAllMocks()
  organizationId = null
  vi.mocked(invitationApi.listPendingInvitations).mockResolvedValue([])
})

describe('RootRedirect', () => {
  it('takes a user without an organization to their pending invitation', async () => {
    vi.mocked(invitationApi.listPendingInvitations).mockResolvedValue([invitation])

    renderRedirect()

    expect(await screen.findByText('aceite')).toBeTruthy()
  })

  it('takes the most recent invitation when there are several', async () => {
    vi.mocked(invitationApi.listPendingInvitations).mockResolvedValue([
      invitation,
      { ...invitation, token: 'tok-antigo' },
    ])

    renderRedirect()

    await screen.findByText('aceite')
    // A primeira da lista é a mais recente: o back-end ordena por criação.
    expect(screen.getByText('aceite')).toBeTruthy()
  })

  it('falls back to the welcome screen when there is no invitation', async () => {
    renderRedirect()

    expect(await screen.findByText('boas-vindas')).toBeTruthy()
  })

  it('sends a user who already has an organization straight to the app', () => {
    organizationId = 'org-1'

    renderRedirect()

    expect(screen.getByText('turmas')).toBeTruthy()
    expect(invitationApi.listPendingInvitations).not.toHaveBeenCalled()
  })

  it('still reaches the welcome screen when the lookup fails', async () => {
    vi.mocked(invitationApi.listPendingInvitations).mockRejectedValue(new Error('boom'))

    renderRedirect()

    await waitFor(() => expect(screen.getByText('boas-vindas')).toBeTruthy())
  })
})
