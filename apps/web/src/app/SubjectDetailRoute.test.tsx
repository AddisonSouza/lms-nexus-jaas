import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import SubjectDetailRoute from './SubjectDetailRoute'

let mockRole: string | null = 'PROFESSOR'

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: mockRole, organizationId: 'org-1' })),
}))

// ADMIN_ORG e GESTOR abrem a seção "Turmas e Professores", que consulta as
// listas da organização: mocka os clients para a rota não sair à rede.
vi.mock('@features/curriculum/api/org-classroom-api', () => ({
  listOrgClassrooms: vi.fn().mockResolvedValue([]),
}))
vi.mock('@features/curriculum/api/org-member-api', () => ({
  listOrgMembers: vi.fn().mockResolvedValue([]),
}))

vi.mock('@features/dashboard/components/ProfessorDashboard', () => ({
  default: ({ subjectId }: { subjectId: string }) => (
    <div data-testid="professor-dashboard">ProfessorDashboard for {subjectId}</div>
  ),
}))

let mockSubject: { name: string; code: string | null } | undefined = {
  name: 'Matemática',
  code: 'MAT1',
}

vi.mock('@features/curriculum/hooks/useSubject', () => ({
  useSubject: () => ({ data: mockSubject }),
}))
vi.mock('@features/curriculum/hooks/useSubjectContents', () => ({
  useSubjectContents: () => ({ data: { topics: [] }, isLoading: false }),
}))
vi.mock('@features/curriculum/hooks/useTopics', () => ({
  useTopics: () => ({ data: [] }),
}))
vi.mock('@features/curriculum/hooks/useCreateTopic', () => ({
  useCreateTopic: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('@features/curriculum/hooks/useUpdateTopic', () => ({
  useUpdateTopic: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('@features/curriculum/hooks/useDeleteTopic', () => ({
  useDeleteTopic: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('@features/curriculum/hooks/useCreateContent', () => ({
  useCreateContent: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('@features/curriculum/hooks/useDeleteContent', () => ({
  useDeleteContent: () => ({ mutate: vi.fn(), isPending: false }),
}))

beforeEach(() => {
  vi.clearAllMocks()
  mockRole = 'PROFESSOR'
  mockSubject = { name: 'Matemática', code: 'MAT1' }
})

function renderRoute(subjectId = 'subject-1') {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={[`/curriculum/${subjectId}`]}>
        <Routes>
          <Route path="/curriculum/:subjectId" element={<SubjectDetailRoute />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('SubjectDetailRoute', () => {
  it('renders the ProfessorDashboard for PROFESSOR users', () => {
    mockRole = 'PROFESSOR'
    renderRoute('subject-1')

    expect(screen.getByTestId('professor-dashboard')).toBeTruthy()
    expect(screen.getByText('ProfessorDashboard for subject-1')).toBeTruthy()
  })

  it.each(['GESTOR', 'ADMIN_ORG'])('renders the ProfessorDashboard for %s users who can teach', (role) => {
    mockRole = role
    renderRoute('subject-1')

    expect(screen.getByTestId('professor-dashboard')).toBeTruthy()
  })

  it('does not render the ProfessorDashboard for ALUNO users', () => {
    mockRole = 'ALUNO'
    renderRoute('subject-1')

    expect(screen.queryByTestId('professor-dashboard')).toBeNull()
  })

  it('names the subject in the header, so the student knows where they are', () => {
    renderRoute()

    expect(screen.getByRole('heading', { name: 'Matemática' })).toBeTruthy()
    expect(screen.getByText('MAT1')).toBeTruthy()
  })

  it('falls back to a generic title while the subject has not arrived', () => {
    mockSubject = undefined

    renderRoute()

    expect(screen.getByRole('heading', { name: 'Conteúdo da Disciplina' })).toBeTruthy()
  })
})
