import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import SubjectListPage from './SubjectListPage'
import * as subjectApi from '../api/subject-api'

vi.mock('../api/subject-api')
const { signedInRole } = vi.hoisted(() => ({ signedInRole: { current: 'ADMIN_ORG' } }))
vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: signedInRole.current })),
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
  teacherUserIds: [],
  createdAt: '2026-08-30T00:00:00',
}

beforeEach(() => {
  vi.clearAllMocks()
  signedInRole.current = 'ADMIN_ORG'
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

  it('keeps the delete dialog open showing why the API refused', async () => {
    const user = userEvent.setup()
    vi.mocked(subjectApi.listSubjects).mockResolvedValue([disciplina])
    vi.mocked(subjectApi.deleteSubject).mockRejectedValue({
      response: { status: 403, data: { error: 'Forbidden' } },
    })

    render(<SubjectListPage />, { wrapper })

    await user.click(await screen.findByRole('button', { name: 'Excluir' }))
    await user.click(screen.getByRole('button', { name: 'Excluir' }))

    const alert = await screen.findByRole('alert')
    expect(alert.textContent).toBe('Você não tem permissão para esta ação.')
    expect(screen.getByText('Excluir disciplina')).toBeTruthy()
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

  // `SubjectResource.delete` é `@RolesAllowed(ADMIN_ORG)`: oferecer o botão ao
  // gestor só rendia 403, com cara de defeito do sistema.
  describe('delete action', () => {
    it('offers it to the organization administrator', async () => {
      vi.mocked(subjectApi.listSubjects).mockResolvedValue([disciplina])

      render(<SubjectListPage />, { wrapper })

      expect(await screen.findByTitle('Excluir')).toBeTruthy()
    })

    it('hides it from the manager, who keeps editing', async () => {
      signedInRole.current = 'GESTOR'
      vi.mocked(subjectApi.listSubjects).mockResolvedValue([disciplina])

      render(<SubjectListPage />, { wrapper })

      expect(await screen.findByTitle('Editar')).toBeTruthy()
      expect(screen.queryByTitle('Excluir')).toBeNull()
    })
  })
})
