import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ClassroomMembersPanel from './ClassroomMembersPanel'
import * as classroomApi from '../api/classroom-api'
import * as orgMemberApi from '../api/org-member-api'
import { classroomKeys } from '../api/query-keys'
import { useAuthStore } from '@store/authStore'
import type { ClassroomMember } from '../types'

vi.mock('../api/classroom-api')
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

const MEMBER: ClassroomMember = {
  id: 'member-1',
  classroomId: 'class-1',
  userId: 'user-1',
  userName: 'Ana Aluna',
  role: 'ALUNO',
  joinedAt: '2026-09-10T10:00:00',
}

const NEW_MEMBER: ClassroomMember = {
  ...MEMBER,
  id: 'member-2',
  userId: '11111111-1111-4111-8111-111111111111',
  userName: 'Bruno Aluno',
}

function renderPanel() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const invalidate = vi.spyOn(queryClient, 'invalidateQueries')

  render(
    <QueryClientProvider client={queryClient}>
      <ClassroomMembersPanel classroomId="class-1" canManage />
    </QueryClientProvider>,
  )

  return { invalidate }
}

async function openAddDialog() {
  await userEvent.click(screen.getByRole('button', { name: /adicionar/i }))
  await waitFor(() => expect(screen.getByLabelText('Pessoa *')).toBeTruthy())
}

async function fillAndSubmit() {
  // Abrir a lista, escolher a pessoa pelo nome e só então o papel: é o caminho
  // do usuário, sem UUID em nenhum momento.
  await userEvent.click(screen.getByLabelText('Pessoa *'))
  await userEvent.click(await screen.findByText('Bruno Aluno'))
  await userEvent.selectOptions(screen.getByLabelText(/papel/i), 'ALUNO')
  await userEvent.click(screen.getAllByRole('button', { name: /^adicionar$/i }).at(-1)!)
}

beforeEach(() => {
  vi.clearAllMocks()
  useAuthStore.setState({ organizationId: 'org-1' })
  vi.mocked(classroomApi.getClassroomMembers).mockResolvedValue([MEMBER])
  vi.mocked(orgMemberApi.searchOrgMembers).mockResolvedValue({
    content: [
      { id: 'om-1', userId: MEMBER.userId, name: 'Ana Aluna', email: 'ana@test.com', role: 'ALUNO' },
      { id: 'om-2', userId: NEW_MEMBER.userId, name: 'Bruno Aluno', email: 'bruno@test.com', role: 'ALUNO' },
    ],
    totalElements: 2,
    totalPages: 1,
    number: 0,
    size: 20,
  })
})

describe('ClassroomMembersPanel', () => {
  it('closes the dialog once the member is added', async () => {
    vi.mocked(classroomApi.addClassroomMember).mockResolvedValue(NEW_MEMBER)

    renderPanel()
    await openAddDialog()
    await fillAndSubmit()

    await waitFor(() => expect(screen.queryByLabelText('Pessoa *')).toBeNull())
  })

  it('invalidates the member list so the table refreshes without a reload', async () => {
    vi.mocked(classroomApi.addClassroomMember).mockResolvedValue(NEW_MEMBER)

    const { invalidate } = renderPanel()
    await openAddDialog()
    await fillAndSubmit()

    await waitFor(() =>
      expect(invalidate).toHaveBeenCalledWith({ queryKey: classroomKeys.members('class-1') }),
    )
  })

  it('keeps the dialog open when the API refuses', async () => {
    vi.mocked(classroomApi.addClassroomMember).mockRejectedValue({
      response: { status: 422, data: { error: 'MEMBER_NOT_IN_ORGANIZATION' } },
    })

    renderPanel()
    await openAddDialog()
    await fillAndSubmit()

    await waitFor(() => expect(classroomApi.addClassroomMember).toHaveBeenCalled())
    expect(screen.getByLabelText('Pessoa *')).toBeTruthy()
  })

  it('disables the submit button while the request is in flight, so a second click cannot add twice', async () => {
    vi.mocked(classroomApi.addClassroomMember).mockImplementation(() => new Promise(() => {}))

    renderPanel()
    await openAddDialog()
    await fillAndSubmit()

    const submit = screen.getAllByRole('button', { name: /adicionar/i }).at(-1)!
    await waitFor(() => expect(submit.hasAttribute('disabled')).toBe(true))
    expect(classroomApi.addClassroomMember).toHaveBeenCalledTimes(1)
  })

  it('offers whoever is already in the classroom as marked, not as a pick', async () => {
    renderPanel()
    await openAddDialog()
    await userEvent.click(screen.getByLabelText('Pessoa *'))

    // Ana já é membro da turma; aparece na busca, mas rotulada.
    expect(await screen.findByText(/já na turma/)).toBeTruthy()
  })
})
