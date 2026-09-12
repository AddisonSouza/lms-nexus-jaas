import { render, screen, waitFor, within } from '@testing-library/react'
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

const pendingInvite = {
  id: 'inv-1',
  email: 'convidado@test.com',
  role: 'ALUNO' as const,
  status: 'PENDING' as const,
  invitedByName: 'Zelia Owner',
  createdAt: '2026-09-10T10:00:00Z',
  expiresAt: '2026-09-17T10:00:00Z',
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(orgApi.listMembers).mockResolvedValue([teacher, owner])
  vi.mocked(orgApi.listInvitations).mockResolvedValue([])
})

function invitationsSection() {
  return screen.getByRole('region', { name: 'Convites' })
}

describe('OrganizationMembersPage — invitations', () => {
  it('lists every invitation with a readable status and who sent it', async () => {
    vi.mocked(orgApi.listInvitations).mockResolvedValue([
      pendingInvite,
      { ...pendingInvite, id: 'inv-2', email: 'aceitou@test.com', status: 'USED' },
      { ...pendingInvite, id: 'inv-3', email: 'venceu@test.com', status: 'EXPIRED', invitedByName: null },
      { ...pendingInvite, id: 'inv-4', email: 'desistiu@test.com', status: 'CANCELLED' },
    ])
    renderPage()

    expect(await screen.findByText('convidado@test.com')).toBeTruthy()
    const section = within(invitationsSection())
    expect(section.getByText('Pendente')).toBeTruthy()
    expect(section.getByText('Aceito')).toBeTruthy()
    expect(section.getByText('Expirado')).toBeTruthy()
    expect(section.getByText('Cancelado')).toBeTruthy()
    expect(section.getAllByText('Aluno')).toHaveLength(4)
    expect(section.getAllByText('Zelia Owner')).toHaveLength(3)
    expect(section.getByText('—')).toBeTruthy()
  })

  it('fetches the invitations of the organization in the route', async () => {
    renderPage()

    await waitFor(() => expect(orgApi.listInvitations).toHaveBeenCalledWith('org-1'))
  })

  it('tells the admin when no invitation was sent', async () => {
    renderPage()

    expect(await screen.findByText('Nenhum convite enviado.')).toBeTruthy()
  })

  it('shows a retryable error for the invitations without hiding the members', async () => {
    vi.mocked(orgApi.listInvitations).mockRejectedValue(new Error('boom'))
    renderPage()

    expect(await screen.findByText(/Não foi possível carregar os convites/)).toBeTruthy()
    expect(screen.getByText('Ana Silva')).toBeTruthy()
  })

  it('resends an invitation with its email and role, then confirms it', async () => {
    vi.mocked(orgApi.listInvitations).mockResolvedValue([pendingInvite])
    vi.mocked(orgApi.inviteMember).mockResolvedValue(undefined)
    renderPage()

    await userEvent.click(await screen.findByLabelText('Reenviar convite para convidado@test.com'))

    await waitFor(() =>
      expect(orgApi.inviteMember).toHaveBeenCalledWith('org-1', {
        email: 'convidado@test.com',
        role: 'ALUNO',
      }),
    )
    expect(await screen.findByText(/Convite enviado para convidado@test.com/)).toBeTruthy()
  })

  it('cancels a pending invitation only after the confirmation', async () => {
    vi.mocked(orgApi.listInvitations).mockResolvedValue([pendingInvite])
    vi.mocked(orgApi.cancelInvitation).mockResolvedValue(undefined)
    renderPage()

    await userEvent.click(await screen.findByLabelText('Cancelar convite para convidado@test.com'))
    expect(orgApi.cancelInvitation).not.toHaveBeenCalled()

    await userEvent.click(screen.getByRole('button', { name: 'Cancelar convite' }))
    await waitFor(() => expect(orgApi.cancelInvitation).toHaveBeenCalledWith('org-1', 'inv-1'))
  })

  it('offers cancel only for a pending invitation and no resend for an accepted one', async () => {
    vi.mocked(orgApi.listInvitations).mockResolvedValue([
      { ...pendingInvite, id: 'inv-2', email: 'aceitou@test.com', status: 'USED' },
      { ...pendingInvite, id: 'inv-3', email: 'venceu@test.com', status: 'EXPIRED' },
    ])
    renderPage()

    expect(await screen.findByLabelText('Reenviar convite para venceu@test.com')).toBeTruthy()
    expect(screen.queryByLabelText('Cancelar convite para venceu@test.com')).toBeNull()
    expect(screen.queryByLabelText('Reenviar convite para aceitou@test.com')).toBeNull()
    expect(screen.queryByLabelText('Cancelar convite para aceitou@test.com')).toBeNull()
  })

  it('reports a failed cancellation on the affected row', async () => {
    vi.mocked(orgApi.listInvitations).mockResolvedValue([pendingInvite])
    vi.mocked(orgApi.cancelInvitation).mockRejectedValue({ response: { status: 409 } })
    renderPage()

    await userEvent.click(await screen.findByLabelText('Cancelar convite para convidado@test.com'))
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar convite' }))

    expect(await screen.findByText('Não foi possível cancelar o convite.')).toBeTruthy()
  })
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

  it('sends the invite with the chosen email and role, then confirms it', async () => {
    vi.mocked(orgApi.inviteMember).mockResolvedValue(undefined)
    renderPage()

    await userEvent.click(await screen.findByRole('button', { name: /Convidar/ }))
    await userEvent.type(screen.getByLabelText('E-mail *'), 'novo@test.com')
    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'PROFESSOR')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar convite' }))

    await waitFor(() =>
      expect(orgApi.inviteMember).toHaveBeenCalledWith('org-1', {
        email: 'novo@test.com',
        role: 'PROFESSOR',
      }),
    )
    expect(await screen.findByRole('status')).toBeTruthy()
    expect(screen.getByText(/Convite enviado para novo@test.com/)).toBeTruthy()
  })

  it('does not send the invite without an email', async () => {
    renderPage()

    await userEvent.click(await screen.findByRole('button', { name: /Convidar/ }))
    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'ALUNO')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar convite' }))

    expect(await screen.findByText('Informe o e-mail')).toBeTruthy()
    expect(orgApi.inviteMember).not.toHaveBeenCalled()
  })

  it('does not send the invite without a role', async () => {
    renderPage()

    await userEvent.click(await screen.findByRole('button', { name: /Convidar/ }))
    await userEvent.type(screen.getByLabelText('E-mail *'), 'novo@test.com')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar convite' }))

    expect(await screen.findByText('Selecione o papel')).toBeTruthy()
    expect(orgApi.inviteMember).not.toHaveBeenCalled()
  })

  it('does not send a malformed email to the API', async () => {
    renderPage()

    await userEvent.click(await screen.findByRole('button', { name: /Convidar/ }))
    await userEvent.type(screen.getByLabelText('E-mail *'), 'nao-e-email')
    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'ALUNO')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar convite' }))

    await waitFor(() => expect(orgApi.inviteMember).not.toHaveBeenCalled())
  })

  it('explains the 409 instead of a generic failure when the email is already a member', async () => {
    vi.mocked(orgApi.inviteMember).mockRejectedValue({ response: { status: 409 } })
    renderPage()

    await userEvent.click(await screen.findByRole('button', { name: /Convidar/ }))
    await userEvent.type(screen.getByLabelText('E-mail *'), 'ana@test.com')
    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'ALUNO')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar convite' }))

    expect(
      await screen.findByText('Esse e-mail já pertence a um membro desta organização.'),
    ).toBeTruthy()
  })

  it('keeps the dialog open with a generic message when the invite fails otherwise', async () => {
    vi.mocked(orgApi.inviteMember).mockRejectedValue({ response: { status: 500 } })
    renderPage()

    await userEvent.click(await screen.findByRole('button', { name: /Convidar/ }))
    await userEvent.type(screen.getByLabelText('E-mail *'), 'novo@test.com')
    await userEvent.selectOptions(screen.getByLabelText('Papel *'), 'ALUNO')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar convite' }))

    expect(
      await screen.findByText('Não foi possível enviar o convite. Tente novamente.'),
    ).toBeTruthy()
    expect(screen.queryByRole('status')).toBeNull()
  })

  it('changes the role of a member through the inline select', async () => {
    vi.mocked(orgApi.changeMemberRole).mockResolvedValue(undefined)
    renderPage()

    await userEvent.selectOptions(await screen.findByLabelText('Papel de Ana Silva'), 'GESTOR')

    await waitFor(() =>
      expect(orgApi.changeMemberRole).toHaveBeenCalledWith('org-1', 'user-1', 'GESTOR'),
    )
  })

  it('shows the current role as the selected option', async () => {
    renderPage()

    const select = (await screen.findByLabelText('Papel de Ana Silva')) as HTMLSelectElement
    expect(select.value).toBe('PROFESSOR')
  })

  it('offers no role select for the organization owner', async () => {
    renderPage()

    await screen.findByText('Zelia Owner')
    expect(screen.queryByLabelText('Papel de Zelia Owner')).toBeNull()
    expect(screen.getByText('Administrador')).toBeTruthy()
  })

  it('shows a role badge instead of a select when the role is not assignable', async () => {
    vi.mocked(orgApi.listMembers).mockResolvedValue([
      { ...teacher, role: 'ADMIN_ORG', owner: false },
    ])
    renderPage()

    expect(await screen.findByText('Administrador')).toBeTruthy()
    expect(screen.queryByLabelText('Papel de Ana Silva')).toBeNull()
  })

  it('reports a failed role change on the affected row', async () => {
    vi.mocked(orgApi.changeMemberRole).mockRejectedValue({ response: { status: 403 } })
    renderPage()

    await userEvent.selectOptions(await screen.findByLabelText('Papel de Ana Silva'), 'ALUNO')

    expect(await screen.findByText('Não foi possível alterar o papel.')).toBeTruthy()
  })

  it('falls back to the user id when identity has no name for the member', async () => {
    vi.mocked(orgApi.listMembers).mockResolvedValue([{ ...teacher, name: null, email: null }])
    renderPage()

    expect(await screen.findByText('user-1')).toBeTruthy()
  })
})
