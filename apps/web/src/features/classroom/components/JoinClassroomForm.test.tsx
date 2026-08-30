import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import JoinClassroomForm from './JoinClassroomForm'
import * as classroomApi from '../api/classroom-api'

vi.mock('../api/classroom-api')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

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

function apiError(code: string, status: number) {
  return { response: { status, data: { error: code } } }
}

async function submitCode(code: string) {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('Código de convite'), code)
  await user.click(screen.getByRole('button', { name: 'Entrar na turma' }))
}

beforeEach(() => vi.clearAllMocks())

describe('JoinClassroomForm', () => {
  it('navigates to the classroom it joined', async () => {
    vi.mocked(classroomApi.joinClassroom).mockResolvedValue({
      id: 'c1',
      name: 'Turma A',
      description: null,
      academicPeriod: '2026.2',
      status: 'ACTIVE',
      inviteCode: null,
      organizationId: 'o1',
      createdAt: '2026-08-30T00:00:00',
    })

    render(<JoinClassroomForm />, { wrapper })
    await submitCode('abc123')

    // O código vai em caixa alta, independente de como foi digitado.
    await waitFor(() => expect(classroomApi.joinClassroom).toHaveBeenCalledWith('ABC123'))
    expect(mockNavigate).toHaveBeenCalledWith('/classrooms/c1')
  })

  it('explains that the code is scoped to the organization', async () => {
    vi.mocked(classroomApi.joinClassroom).mockRejectedValue(apiError('INVALID_INVITE_CODE', 404))

    render(<JoinClassroomForm />, { wrapper })
    await submitCode('ZZZZZZ')

    expect(await screen.findByText(/vale só dentro da sua organização/)).toBeTruthy()
  })

  it('tells the user when the classroom is archived', async () => {
    vi.mocked(classroomApi.joinClassroom).mockRejectedValue(apiError('CLASSROOM_ARCHIVED', 422))

    render(<JoinClassroomForm />, { wrapper })
    await submitCode('OLD123')

    expect(await screen.findByText(/arquivada/)).toBeTruthy()
  })

  it('falls back to a generic message for an unmapped failure', async () => {
    vi.mocked(classroomApi.joinClassroom).mockRejectedValue(new Error('network down'))

    render(<JoinClassroomForm />, { wrapper })
    await submitCode('ABC123')

    expect(await screen.findByText(/Tente de novo em instantes/)).toBeTruthy()
  })
})
