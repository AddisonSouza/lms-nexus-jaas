import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import SubjectDetailPage from './SubjectDetailPage'
import * as orgClassroomApi from '../api/org-classroom-api'
import * as orgMemberApi from '../api/org-member-api'
import { useAuthStore } from '@store/authStore'

vi.mock('../api/org-classroom-api')
vi.mock('../api/org-member-api')

// O diálogo de atribuir professor usa combobox, e o jsdom não traz nada disso
// que o Positioner do base-ui precisa.
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

let mockSubject:
  | { classroomIds: string[]; teacherMemberIds: string[]; teacherUserIds: string[] }
  | undefined = { classroomIds: [], teacherMemberIds: [], teacherUserIds: [] }

vi.mock('../hooks/useSubject', () => ({
  useSubject: () => ({ data: mockSubject }),
}))
vi.mock('../hooks/useSubjectContents', () => ({
  useSubjectContents: () => ({ data: { topics: [] }, isLoading: false }),
}))
vi.mock('../hooks/useTopics', () => ({ useTopics: () => ({ data: [] }) }))
vi.mock('../hooks/useCreateTopic', () => ({
  useCreateTopic: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('../hooks/useUpdateTopic', () => ({
  useUpdateTopic: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('../hooks/useDeleteTopic', () => ({
  useDeleteTopic: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('../hooks/useCreateContent', () => ({
  useCreateContent: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('../hooks/useDeleteContent', () => ({
  useDeleteContent: () => ({ mutate: vi.fn(), isPending: false }),
}))

const linkMutate = vi.fn()
const unlinkMutate = vi.fn()
const assignMutate = vi.fn()
const removeTeacherMutate = vi.fn()

vi.mock('../hooks/useLinkClassroom', () => ({
  useLinkClassroom: () => ({ mutate: linkMutate, isPending: false, isError: false, reset: vi.fn() }),
}))
vi.mock('../hooks/useUnlinkClassroom', () => ({
  useUnlinkClassroom: () => ({ mutate: unlinkMutate, isPending: false, isError: false, reset: vi.fn() }),
}))
vi.mock('../hooks/useAssignTeacher', () => ({
  useAssignTeacher: () => ({ mutate: assignMutate, isPending: false, isError: false, reset: vi.fn() }),
}))
vi.mock('../hooks/useRemoveTeacher', () => ({
  useRemoveTeacher: () => ({ mutate: removeTeacherMutate, isPending: false, isError: false, reset: vi.fn() }),
}))

const CLASSROOMS = [
  { id: 'c-1', name: 'Turma Vinculada', status: 'ACTIVE' as const },
  { id: 'c-2', name: 'Turma Arquivada', status: 'ARCHIVED' as const },
  { id: 'c-3', name: 'Turma Livre', status: 'ACTIVE' as const },
]

const MEMBERS = [
  { id: 'm-1', userId: 'u-1', name: 'Ana Professora', email: 'ana@test.com', role: 'PROFESSOR' as const },
  { id: 'm-2', userId: 'u-2', name: 'Bruno Gestor', email: 'bruno@test.com', role: 'GESTOR' as const },
]

function renderPage(role: string, userId = 'u-outro') {
  useAuthStore.setState({ role, userId, organizationId: 'org-1' })
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/curriculum/subject-1']}>
        <Routes>
          <Route
            path="/curriculum/:subjectId"
            element={<SubjectDetailPage dashboardSlot={<div>painel</div>} />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  mockSubject = { classroomIds: [], teacherMemberIds: [], teacherUserIds: [] }
  vi.mocked(orgClassroomApi.listOrgClassrooms).mockResolvedValue(CLASSROOMS)
  vi.mocked(orgMemberApi.listOrgMembers).mockResolvedValue({
    content: MEMBERS,
    totalElements: MEMBERS.length,
    totalPages: 1,
    number: 0,
    size: 20,
  })
})

describe('SubjectDetailPage — Turmas e Professores', () => {
  it.each(['ADMIN_ORG', 'GESTOR'])('shows the section for %s', (role) => {
    renderPage(role)
    expect(screen.getByRole('heading', { name: /turmas e professores/i })).toBeTruthy()
  })

  it.each(['PROFESSOR', 'ALUNO'])('hides the section from %s', (role) => {
    renderPage(role)
    expect(screen.queryByRole('heading', { name: /turmas e professores/i })).toBeNull()
  })

  it('names the linked classrooms and teachers instead of showing ids', async () => {
    mockSubject = { classroomIds: ['c-1'], teacherMemberIds: ['m-1'], teacherUserIds: ['u-1'] }
    renderPage('ADMIN_ORG')

    await waitFor(() => expect(screen.getByText('Turma Vinculada')).toBeTruthy())
    expect(screen.getByText('Ana Professora')).toBeTruthy()
    expect(screen.queryByText('c-1')).toBeNull()
    expect(screen.queryByText('m-1')).toBeNull()
  })

  it('flags a linked classroom that was archived afterwards', async () => {
    mockSubject = { classroomIds: ['c-2'], teacherMemberIds: [], teacherUserIds: [] }
    renderPage('GESTOR')

    await waitFor(() => expect(screen.getByText('Turma Arquivada')).toBeTruthy())
    expect(screen.getByText('Arquivada')).toBeTruthy()
  })

  it('tells the admin what is missing when nothing is linked', () => {
    renderPage('ADMIN_ORG')

    expect(screen.getByText(/nenhuma turma vinculada/i)).toBeTruthy()
    expect(screen.getByText(/nenhum professor atribuído/i)).toBeTruthy()
  })

  it('links a classroom chosen from the list', async () => {
    renderPage('ADMIN_ORG')

    await userEvent.click(screen.getByRole('button', { name: /vincular turma/i }))
    await waitFor(() => expect(screen.getByRole('option', { name: 'Turma Livre' })).toBeTruthy())
    await userEvent.selectOptions(screen.getByLabelText(/turma \*/i), 'c-3')
    await userEvent.click(screen.getByRole('button', { name: /^vincular$/i }))

    await waitFor(() =>
      expect(linkMutate).toHaveBeenCalledWith({ classroomId: 'c-3' }, expect.any(Object)),
    )
  })

  it('asks for confirmation before unlinking a classroom', async () => {
    mockSubject = { classroomIds: ['c-1'], teacherMemberIds: [], teacherUserIds: [] }
    renderPage('ADMIN_ORG')

    await waitFor(() => expect(screen.getByText('Turma Vinculada')).toBeTruthy())
    await userEvent.click(screen.getByRole('button', { name: /desvincular turma vinculada/i }))

    await waitFor(() => expect(screen.getByText(/desvincular "Turma Vinculada"/i)).toBeTruthy())
    expect(unlinkMutate).not.toHaveBeenCalled()

    await userEvent.click(screen.getByRole('button', { name: /^desvincular$/i }))
    await waitFor(() => expect(unlinkMutate).toHaveBeenCalledWith('c-1', expect.any(Object)))
  })

  it('asks for confirmation before removing a teacher', async () => {
    mockSubject = { classroomIds: [], teacherMemberIds: ['m-1'], teacherUserIds: ['u-1'] }
    renderPage('GESTOR')

    await waitFor(() => expect(screen.getByText('Ana Professora')).toBeTruthy())
    await userEvent.click(screen.getByRole('button', { name: /remover ana professora/i }))

    await waitFor(() => expect(screen.getByText(/remover "Ana Professora"/i)).toBeTruthy())
    expect(removeTeacherMutate).not.toHaveBeenCalled()

    await userEvent.click(screen.getByRole('button', { name: /^remover$/i }))
    await waitFor(() => expect(removeTeacherMutate).toHaveBeenCalledWith('m-1', expect.any(Object)))
  })

  it('assigns a teacher chosen from the list', async () => {
    renderPage('ADMIN_ORG')

    await userEvent.click(screen.getByRole('button', { name: /atribuir professor/i }))
    // O combobox só monta a lista depois que o campo é clicado.
    await userEvent.click(await screen.findByLabelText(/membro \*/i))
    await userEvent.click(await screen.findByText('Bruno Gestor'))
    await userEvent.click(screen.getByRole('button', { name: /^atribuir$/i }))

    await waitFor(() =>
      expect(assignMutate).toHaveBeenCalledWith({ memberId: 'm-2' }, expect.any(Object)),
    )
  })
})

// `GetProfessorDashboardService` só devolve dados para quem leciona a disciplina.
// Antes o bloco saía pelo papel na organização, e gestor/admin ficavam com o
// cabeçalho "Dashboard da Disciplina" sobre um bloco vazio.
describe('SubjectDetailPage — Dashboard da Disciplina', () => {
  it('shows the block to the teacher assigned to this subject', () => {
    mockSubject = { classroomIds: [], teacherMemberIds: ['m-1'], teacherUserIds: ['u-1'] }

    renderPage('PROFESSOR', 'u-1')

    expect(screen.getByRole('heading', { name: /dashboard da disciplina/i })).toBeTruthy()
  })

  it('hides it from a professor who does not teach this subject', () => {
    mockSubject = { classroomIds: [], teacherMemberIds: ['m-1'], teacherUserIds: ['u-1'] }

    renderPage('PROFESSOR', 'u-outro')

    expect(screen.queryByRole('heading', { name: /dashboard da disciplina/i })).toBeNull()
  })

  it.each(['ADMIN_ORG', 'GESTOR'])('hides it from %s, who does not teach', (role) => {
    mockSubject = { classroomIds: [], teacherMemberIds: ['m-1'], teacherUserIds: ['u-1'] }

    renderPage(role, 'u-admin')

    expect(screen.queryByRole('heading', { name: /dashboard da disciplina/i })).toBeNull()
  })
})
