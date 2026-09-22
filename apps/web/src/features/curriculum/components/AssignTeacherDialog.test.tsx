import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import AssignTeacherDialog from './AssignTeacherDialog'
import * as orgMemberApi from '../api/org-member-api'
import { useAuthStore } from '@store/authStore'

vi.mock('../api/org-member-api')

const MEMBERS = [
  { id: 'm-1', userId: 'u-1', name: 'Ana Professora', email: 'ana@test.com', role: 'PROFESSOR' as const },
  { id: 'm-2', userId: 'u-2', name: 'Bruno Gestor', email: 'bruno@test.com', role: 'GESTOR' as const },
  { id: 'm-3', userId: 'u-3', name: 'Duda Aluna', email: 'duda@test.com', role: 'ALUNO' as const },
]

function renderDialog(props: Partial<React.ComponentProps<typeof AssignTeacherDialog>> = {}) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <AssignTeacherDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        assignedMemberIds={[]}
        {...props}
      />
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  useAuthStore.setState({ organizationId: 'org-1' })
  vi.mocked(orgMemberApi.listOrgMembers).mockResolvedValue({
    content: MEMBERS,
    totalElements: MEMBERS.length,
    totalPages: 1,
    number: 0,
    size: 20,
  })
})

describe('AssignTeacherDialog', () => {
  it('lists only eligible members that are not assigned yet', async () => {
    renderDialog({ assignedMemberIds: ['m-2'] })

    await waitFor(() =>
      expect(screen.getByRole('option', { name: /ana professora/i })).toBeTruthy(),
    )
    expect(screen.queryByRole('option', { name: /bruno gestor/i })).toBeNull()
    expect(screen.queryByRole('option', { name: /duda aluna/i })).toBeNull()
  })

  it('submits the membership id, not the user id', async () => {
    const onSubmit = vi.fn()
    renderDialog({ onSubmit })

    await waitFor(() =>
      expect(screen.getByRole('option', { name: /ana professora/i })).toBeTruthy(),
    )
    await userEvent.selectOptions(screen.getByLabelText(/membro \*/i), 'm-1')
    await userEvent.click(screen.getByRole('button', { name: /atribuir/i }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith('m-1'))
  })

  it('does not submit without a choice', async () => {
    const onSubmit = vi.fn()
    renderDialog({ onSubmit })

    await waitFor(() =>
      expect(screen.getByRole('option', { name: /ana professora/i })).toBeTruthy(),
    )
    await userEvent.click(screen.getByRole('button', { name: /atribuir/i }))

    await waitFor(() => expect(screen.getByText('Escolha um membro')).toBeTruthy())
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('shows the API refusal without closing', async () => {
    renderDialog({ error: 'Este membro não pode lecionar.' })

    await waitFor(() => expect(screen.getByText('Este membro não pode lecionar.')).toBeTruthy())
  })

  it('explains when every eligible member is already assigned', async () => {
    renderDialog({ assignedMemberIds: ['m-1', 'm-2'] })

    await waitFor(() =>
      expect(screen.getByText(/nenhum membro disponível/i)).toBeTruthy(),
    )
    expect(screen.getByRole('button', { name: /atribuir/i }).hasAttribute('disabled')).toBe(true)
  })
})
