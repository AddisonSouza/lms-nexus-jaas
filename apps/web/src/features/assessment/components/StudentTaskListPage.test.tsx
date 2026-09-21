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

const submissionEnviada = {
  id: 'sub-id-1',
  status: 'SUBMITTED' as const,
  grade: null,
  feedback: null,
  submittedAt: '2026-09-10T12:00:00',
  lateSubmission: false,
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

  it('offers "Editar resposta" for a SUBMITTED task before the deadline', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([
      { ...tarefa, submission: { ...submissionEnviada } },
    ])

    render(<StudentTaskListPage />, { wrapper })

    expect(await screen.findByRole('button', { name: /Editar resposta/ })).toBeTruthy()
  })

  it('hides "Editar resposta" once the deadline has passed', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([
      { ...tarefa, deadline: '2020-01-01T23:59:00', submission: { ...submissionEnviada } },
    ])

    render(<StudentTaskListPage />, { wrapper })

    await screen.findByText('Trabalho de Álgebra')
    expect(screen.queryByRole('button', { name: /Editar resposta/ })).toBeNull()
  })

  it('hides "Editar resposta" once the submission is evaluated', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([
      { ...tarefa, submission: { ...submissionEnviada, status: 'EVALUATED', grade: 9 } },
    ])

    render(<StudentTaskListPage />, { wrapper })

    await screen.findByText('Trabalho de Álgebra')
    expect(screen.queryByRole('button', { name: /Editar resposta/ })).toBeNull()
  })

  it('marks the task as Encerrada once the API reports CLOSED', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([
      { ...tarefa, deadline: '2020-01-01T23:59:00', status: 'CLOSED' },
    ])

    render(<StudentTaskListPage />, { wrapper })

    expect(await screen.findByText('Encerrada')).toBeTruthy()
  })

  it('keeps the grade of a closed task reachable', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([
      {
        ...tarefa,
        deadline: '2020-01-01T23:59:00',
        status: 'CLOSED',
        submission: { ...submissionEnviada, status: 'EVALUATED', grade: 9 },
      },
    ])

    render(<StudentTaskListPage />, { wrapper })

    expect(await screen.findByText('Encerrada')).toBeTruthy()
    expect(screen.getByText('Nota: 9 / 10')).toBeTruthy()
    expect(screen.getByRole('button', { name: /Ver Nota/ })).toBeTruthy()
  })

  it('does not show Encerrada while the task is still open', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([tarefa])

    render(<StudentTaskListPage />, { wrapper })

    await screen.findByText('Trabalho de Álgebra')
    expect(screen.queryByText('Encerrada')).toBeNull()
  })

  it('sends the edited answer through updateSubmission', async () => {
    vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([
      { ...tarefa, submission: { ...submissionEnviada } },
    ])
    vi.mocked(submissionsApi.updateSubmission).mockResolvedValue({} as never)

    render(<StudentTaskListPage />, { wrapper })

    await userEvent.click(await screen.findByRole('button', { name: /Editar resposta/ }))
    await userEvent.type(screen.getByRole('textbox'), 'resposta corrigida')
    await userEvent.click(screen.getByRole('button', { name: /Salvar Resposta/ }))

    // o TanStack Query passa um segundo argumento à mutationFn; só o payload importa
    await waitFor(() => expect(submissionsApi.updateSubmission).toHaveBeenCalled())
    expect(vi.mocked(submissionsApi.updateSubmission).mock.calls[0][0]).toMatchObject({
      taskId: 'task-1',
      submissionId: 'sub-id-1',
      textResponse: 'resposta corrigida',
    })
  })

  // Os anexos do enunciado já vinham no `my-grades` e nunca eram renderizados:
  // o aluno via a tarefa mas não tinha como chegar ao arquivo.
  describe('task attachments', () => {
    const comAnexo: TaskWithGrade = {
      ...tarefa,
      attachments: [
        { id: 'a-1', fileKey: 'task_attachment/2026/09/uuid-enunciado.pdf', originalName: 'enunciado.pdf', mimeType: 'application/pdf', sizeBytes: 2048 },
      ],
    }

    it('offers the task attachment for download', async () => {
      vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([comAnexo])

      render(<StudentTaskListPage />, { wrapper })

      expect(await screen.findByRole('button', { name: /baixar enunciado\.pdf/i })).toBeTruthy()
      expect(screen.getByText('(2,0 KB)')).toBeTruthy()
    })

    it('shows no attachment row when the task has none', async () => {
      vi.mocked(submissionsApi.listStudentGrades).mockResolvedValue([tarefa])

      render(<StudentTaskListPage />, { wrapper })

      await screen.findByText('Trabalho de Álgebra')
      expect(screen.queryByRole('button', { name: /baixar/i })).toBeNull()
    })
  })
})
