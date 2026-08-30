import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import SubjectListPage from './SubjectListPage'
import * as subjectApi from '../api/subject-api'

vi.mock('../api/subject-api')
vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: 'ADMIN_ORG' })),
}))

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return (
    <QueryClientProvider client={qc}>
      <MemoryRouter>{children}</MemoryRouter>
    </QueryClientProvider>
  )
}

const disciplina = {
  id: 's1',
  name: 'Matemática',
  code: 'MAT1',
  description: null,
  workloadHours: 60,
  organizationId: 'o1',
  classroomIds: [],
  teacherMemberIds: [],
  createdAt: '2026-08-30T00:00:00',
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('SubjectListPage', () => {
  it('lists the subjects it receives', async () => {
    vi.mocked(subjectApi.listSubjects).mockResolvedValue([disciplina])

    render(<SubjectListPage />, { wrapper })

    expect(await screen.findByText('Matemática')).toBeTruthy()
    expect(screen.getByText('MAT1')).toBeTruthy()
  })

  it('shows the empty state only when the request succeeded with no subjects', async () => {
    vi.mocked(subjectApi.listSubjects).mockResolvedValue([])

    render(<SubjectListPage />, { wrapper })

    expect(await screen.findByText('Nenhuma disciplina encontrada.')).toBeTruthy()
  })

  it('reports the failure instead of an empty list', async () => {
    vi.mocked(subjectApi.listSubjects).mockRejectedValue(new Error('403'))

    render(<SubjectListPage />, { wrapper })

    expect(await screen.findByText('Não foi possível carregar as disciplinas')).toBeTruthy()
    expect(screen.queryByText('Nenhuma disciplina encontrada.')).toBeNull()
    expect(screen.queryByRole('table')).toBeNull()
  })

  it('refetches when the user retries', async () => {
    const user = userEvent.setup()
    vi.mocked(subjectApi.listSubjects)
      .mockRejectedValueOnce(new Error('503'))
      .mockResolvedValueOnce([disciplina])

    render(<SubjectListPage />, { wrapper })

    await user.click(await screen.findByRole('button', { name: 'Tentar de novo' }))

    await waitFor(() => expect(screen.getByText('Matemática')).toBeTruthy())
  })
})
