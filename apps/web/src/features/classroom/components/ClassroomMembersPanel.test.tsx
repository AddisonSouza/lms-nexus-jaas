import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ClassroomMembersPanel from './ClassroomMembersPanel'
import * as classroomApi from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'
import type { ClassroomMember } from '../types'

vi.mock('../api/classroom-api')

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
  await waitFor(() => expect(screen.getByLabelText(/id do usuário/i)).toBeTruthy())
}

async function fillAndSubmit() {
  await userEvent.type(screen.getByLabelText(/id do usuário/i), NEW_MEMBER.userId)
  await userEvent.selectOptions(screen.getByLabelText(/papel/i), 'ALUNO')
  await userEvent.click(screen.getByRole('button', { name: /^adicionar$/i, hidden: false }))
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(classroomApi.getClassroomMembers).mockResolvedValue([MEMBER])
})

describe('ClassroomMembersPanel', () => {
  it('closes the dialog once the member is added', async () => {
    vi.mocked(classroomApi.addClassroomMember).mockResolvedValue(NEW_MEMBER)

    renderPanel()
    await openAddDialog()
    await fillAndSubmit()

    await waitFor(() => expect(screen.queryByLabelText(/id do usuário/i)).toBeNull())
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
    expect(screen.getByLabelText(/id do usuário/i)).toBeTruthy()
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
})
