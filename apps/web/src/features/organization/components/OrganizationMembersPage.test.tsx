import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import OrganizationMembersPage from './OrganizationMembersPage'
import * as orgApi from '../api/organization-api'

vi.mock('../api/organization-api')

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/organizations/org-1/members']}>
        <Routes>
          <Route path="/organizations/:id/members" element={<OrganizationMembersPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const owner = {
  id: 'm-owner',
  userId: 'user-owner',
  name: 'Zelia Owner',
  email: 'zelia@test.com',
  role: 'ADMIN_ORG' as const,
  joinedAt: '2026-01-01T10:00:00',
  owner: true,
}

const teacher = {
  id: 'm-1',
  userId: 'user-1',
  name: 'Ana Silva',
  email: 'ana@test.com',
  role: 'PROFESSOR' as const,
  joinedAt: '2026-02-15T10:00:00',
  owner: false,
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(orgApi.listMembers).mockResolvedValue([teacher, owner])
})

describe('OrganizationMembersPage', () => {
  it('lists the members with name, email and a readable role', async () => {
    renderPage()

    expect(await screen.findByText('Ana Silva')).toBeTruthy()
    expect(screen.getByText('ana@test.com')).toBeTruthy()
    expect(screen.getByText('Professor')).toBeTruthy()
    expect(screen.getByText('Zelia Owner')).toBeTruthy()
    expect(screen.getByText('Administrador')).toBeTruthy()
  })

  it('fetches the members of the organization in the route', async () => {
    renderPage()

    await screen.findByText('Ana Silva')
    expect(orgApi.listMembers).toHaveBeenCalledWith('org-1')
  })

  it('offers no remove action for the organization owner', async () => {
    renderPage()

    await screen.findByText('Zelia Owner')
    expect(screen.getByText('Criador')).toBeTruthy()
    expect(screen.queryByLabelText('Remover Zelia Owner')).toBeNull()
    expect(screen.getByLabelText('Remover Ana Silva')).toBeTruthy()
  })

  it('removes a member only after the confirmation', async () => {
    vi.mocked(orgApi.removeMember).mockResolvedValue(undefined)
    renderPage()

    await userEvent.click(await screen.findByLabelText('Remover Ana Silva'))
    expect(orgApi.removeMember).not.toHaveBeenCalled()

    await userEvent.click(screen.getByRole('button', { name: 'Remover' }))
    await waitFor(() => expect(orgApi.removeMember).toHaveBeenCalledWith('org-1', 'user-1'))
  })

  it('keeps the member when the confirmation is cancelled', async () => {
    renderPage()

    await userEvent.click(await screen.findByLabelText('Remover Ana Silva'))
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(orgApi.removeMember).not.toHaveBeenCalled()
  })

  it('shows a retryable error state instead of an empty table when the request fails', async () => {
    vi.mocked(orgApi.listMembers).mockRejectedValue(new Error('boom'))
    renderPage()

    expect(await screen.findByRole('alert')).toBeTruthy()
    expect(screen.getByText(/Não foi possível carregar os membros/)).toBeTruthy()
  })

  it('tells the admin when the organization has no members', async () => {
    vi.mocked(orgApi.listMembers).mockResolvedValue([])
    renderPage()

    expect(await screen.findByText('Nenhum membro nesta organização.')).toBeTruthy()
  })

  it('falls back to the user id when identity has no name for the member', async () => {
    vi.mocked(orgApi.listMembers).mockResolvedValue([{ ...teacher, name: null, email: null }])
    renderPage()

    expect(await screen.findByText('user-1')).toBeTruthy()
  })
})
