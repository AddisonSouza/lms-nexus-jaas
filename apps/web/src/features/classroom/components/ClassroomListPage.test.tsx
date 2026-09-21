import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import ClassroomListPage from './ClassroomListPage'
import * as classroomApi from '../api/classroom-api'

vi.mock('../api/classroom-api')
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

const turma = {
  id: 'c1',
  name: 'Turma A',
  description: null,
  academicPeriod: '2026.2',
  status: 'ACTIVE' as const,
  inviteCode: 'ABC123',
  organizationId: 'o1',
  createdAt: '2026-08-30T00:00:00',
}

beforeEach(() => {
  vi.clearAllMocks()
  signedInRole.current = 'ADMIN_ORG'
})

describe('ClassroomListPage', () => {
  it('lists the classrooms it receives', async () => {
    vi.mocked(classroomApi.getClassrooms).mockResolvedValue([turma])

    render(<ClassroomListPage />, { wrapper })

    expect(await screen.findByText('Turma A')).toBeTruthy()
    expect(screen.getByText('2026.2')).toBeTruthy()
  })

  it('shows the empty state only when the request succeeded with no classrooms', async () => {
    vi.mocked(classroomApi.getClassrooms).mockResolvedValue([])

    render(<ClassroomListPage />, { wrapper })

    expect(await screen.findByText('Nenhuma turma encontrada.')).toBeTruthy()
  })

  it('reports the failure instead of an empty list', async () => {
    vi.mocked(classroomApi.getClassrooms).mockRejectedValue(new Error('403'))

    render(<ClassroomListPage />, { wrapper })

    expect(await screen.findByText('Não foi possível carregar as turmas')).toBeTruthy()
    expect(screen.queryByText('Nenhuma turma encontrada.')).toBeNull()
    expect(screen.queryByRole('table')).toBeNull()
  })

  it('refetches when the user retries', async () => {
    const user = userEvent.setup()
    vi.mocked(classroomApi.getClassrooms)
      .mockRejectedValueOnce(new Error('503'))
      .mockResolvedValueOnce([turma])

    render(<ClassroomListPage />, { wrapper })

    await user.click(await screen.findByRole('button', { name: 'Tentar de novo' }))

    await waitFor(() => expect(screen.getByText('Turma A')).toBeTruthy())
  })

  // Entrar por código é ação de aluno: para os demais papéis o botão abria um
  // formulário inline que nunca servia para nada.
  describe('join by code', () => {
    it('offers it to the student', async () => {
      signedInRole.current = 'ALUNO'
      vi.mocked(classroomApi.getClassrooms).mockResolvedValue([turma])

      render(<ClassroomListPage />, { wrapper })

      expect(await screen.findByRole('button', { name: /entrar via código/i })).toBeTruthy()
    })

    it('hides the button and its inline form from the administrator', async () => {
      vi.mocked(classroomApi.getClassrooms).mockResolvedValue([turma])

      render(<ClassroomListPage />, { wrapper })

      await screen.findByText('Turma A')
      expect(screen.queryByRole('button', { name: /entrar via código/i })).toBeNull()
      expect(screen.queryByLabelText(/código/i)).toBeNull()
    })
  })
})
