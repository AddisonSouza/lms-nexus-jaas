import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import SubjectDetailPage from './SubjectDetailPage'

let mockRole: string | null = 'PROFESSOR'

vi.mock('@features/auth/store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: mockRole })),
}))

vi.mock('@features/dashboard/components/ProfessorDashboard', () => ({
  default: ({ subjectId }: { subjectId: string }) => (
    <div data-testid="professor-dashboard">ProfessorDashboard for {subjectId}</div>
  ),
}))

vi.mock('../hooks/useSubjectContents', () => ({
  useSubjectContents: () => ({ data: { topics: [] }, isLoading: false }),
}))
vi.mock('../hooks/useTopics', () => ({
  useTopics: () => ({ data: [] }),
}))
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

beforeEach(() => {
  vi.clearAllMocks()
  mockRole = 'PROFESSOR'
})

function renderPage(subjectId = 'subject-1') {
  return render(
    <MemoryRouter initialEntries={[`/curriculum/${subjectId}`]}>
      <Routes>
        <Route path="/curriculum/:subjectId" element={<SubjectDetailPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('SubjectDetailPage', () => {
  it('renders the ProfessorDashboard for PROFESSOR users', () => {
    mockRole = 'PROFESSOR'
    renderPage('subject-1')

    expect(screen.getByTestId('professor-dashboard')).toBeTruthy()
    expect(screen.getByText('ProfessorDashboard for subject-1')).toBeTruthy()
  })

  it('does not render the ProfessorDashboard for non-PROFESSOR roles', () => {
    mockRole = 'ALUNO'
    renderPage('subject-1')

    expect(screen.queryByTestId('professor-dashboard')).toBeNull()
  })
})
