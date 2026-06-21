import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import SubjectDetailRoute from './SubjectDetailRoute'

let mockRole: string | null = 'PROFESSOR'

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: mockRole })),
}))

vi.mock('@features/dashboard/components/ProfessorDashboard', () => ({
  default: ({ subjectId }: { subjectId: string }) => (
    <div data-testid="professor-dashboard">ProfessorDashboard for {subjectId}</div>
  ),
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
})

function renderRoute(subjectId = 'subject-1') {
  return render(
    <MemoryRouter initialEntries={[`/curriculum/${subjectId}`]}>
      <Routes>
        <Route path="/curriculum/:subjectId" element={<SubjectDetailRoute />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('SubjectDetailRoute', () => {
  it('renders the ProfessorDashboard for PROFESSOR users', () => {
    mockRole = 'PROFESSOR'
    renderRoute('subject-1')

    expect(screen.getByTestId('professor-dashboard')).toBeTruthy()
    expect(screen.getByText('ProfessorDashboard for subject-1')).toBeTruthy()
  })

  it('does not render the ProfessorDashboard for non-PROFESSOR roles', () => {
    mockRole = 'ALUNO'
    renderRoute('subject-1')

    expect(screen.queryByTestId('professor-dashboard')).toBeNull()
  })
})
