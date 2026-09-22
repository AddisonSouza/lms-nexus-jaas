import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import AssignTeacherDialog from './AssignTeacherDialog'
import * as orgMemberApi from '../api/org-member-api'
import { useAuthStore } from '@store/authStore'

vi.mock('../api/org-member-api')

// O jsdom não traz nada disso, e o Positioner do combobox usa os três.
beforeAll(() => {
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    }
  )
  Element.prototype.scrollIntoView = vi.fn()
  if (!Element.prototype.hasPointerCapture) {
    Element.prototype.hasPointerCapture = () => false
  }
})

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

/** A lista só monta depois que o campo é clicado — é assim que o usuário a abre. */
async function openList() {
  await userEvent.click(screen.getByLabelText(/membro \*/i))
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
    await openList()

    expect(await screen.findByText('Ana Professora')).toBeTruthy()
    // Bruno já está atribuído; Duda é ALUNO e a API recusaria.
    expect(screen.queryByText('Bruno Gestor')).toBeNull()
    expect(screen.queryByText('Duda Aluna')).toBeNull()
  })

  it('searches on the API by name or email instead of filtering on the client', async () => {
    renderDialog()
    await openList()
    await screen.findByText('Ana Professora')

    await userEvent.type(screen.getByLabelText(/membro \*/i), 'ana')

    await waitFor(() =>
      expect(orgMemberApi.listOrgMembers).toHaveBeenCalledWith(
        'org-1',
        expect.objectContaining({ search: 'ana' })
      )
    )
  })

  it('submits the membership id, not the user id', async () => {
    const onSubmit = vi.fn()
    renderDialog({ onSubmit })
    await openList()

    await userEvent.click(await screen.findByText('Ana Professora'))
    await userEvent.click(screen.getByRole('button', { name: /atribuir/i }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith('m-1'))
  })

  it('does not submit without a choice', async () => {
    const onSubmit = vi.fn()
    renderDialog({ onSubmit })

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

    await waitFor(() => expect(screen.getByText(/nenhum membro disponível/i)).toBeTruthy())
    expect(screen.getByRole('button', { name: /atribuir/i }).hasAttribute('disabled')).toBe(true)
  })
})
