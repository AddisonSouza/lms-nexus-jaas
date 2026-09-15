import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import StudentTaskListPage from './StudentTaskListPage'
import * as submissionsApi from '../api/submissions'
import type { TaskWithGrade } from '../types'

vi.mock('../api/submissions')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>
}

const tarefa: TaskWithGrade = {
  id: 'task-1',
  subjectId: 'sub-1',
  organizationId: 'org-1',
  createdBy: 'user-1',
  title: 'Trabalho de Álgebra',
  description: 'Resolva os exercícios',
  deadline: '2027-12-31T23:59:00',
  maxScore: 10,
  status: 'PUBLISHED',
  attachments: [],
  createdAt: '2026-09-01T10:00:00',
  submission: null,
}

beforeEach(() => vi.clearAllMocks())

describe('StudentTaskListPage', () => {
  it('lists the tasks returned by my-grades', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([tarefa])

    render(<StudentTaskListPage />, { wrapper })

    expect(await screen.findByText('Trabalho de Álgebra')).toBeTruthy()
  })

  it('shows the error state instead of an empty list when the query fails', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockRejectedValue(new Error('boom'))

    render(<StudentTaskListPage />, { wrapper })

    expect(await screen.findByRole('alert')).toBeTruthy()
    expect(screen.getByText('Não foi possível carregar as tarefas')).toBeTruthy()
    expect(screen.queryByText('Nenhuma tarefa disponível no momento.')).toBeNull()
  })

  it('refetches when the user hits retry', async () => {
    vi.mocked(submissionsApi.listStudentGrades)
      .mockRejectedValueOnce(new Error('boom'))
      .mockResolvedValueOnce([tarefa])

    render(<StudentTaskListPage />, { wrapper })

    await userEvent.click(await screen.findByRole('button', { name: /Tentar de novo/ }))

    await waitFor(() => expect(screen.getByText('Trabalho de Álgebra')).toBeTruthy())
  })
})
