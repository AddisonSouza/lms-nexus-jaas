import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import SubmissionListDrawer from './SubmissionListDrawer'
import * as submissionsApi from '../api/submissions'
import { submissionKeys } from '../api/query-keys'
import type { Task, TaskSubmission } from '../types'

vi.mock('../api/submissions')

const TASK: Task = {
  id: 'task-1',
  subjectId: 'sub-1',
  organizationId: 'org-1',
  createdBy: 'prof-1',
  title: 'Lista de exercícios',
  description: 'Enunciado',
  deadline: new Date(Date.now() + 86_400_000).toISOString(),
  maxScore: 10,
  status: 'PUBLISHED',
  attachments: [],
  createdAt: '2026-09-01T10:00:00',
  updatedAt: null,
}

const SUBMISSION: TaskSubmission = {
  id: 'sub-100',
  taskId: 'task-1',
  studentId: 'student-1',
  organizationId: 'org-1',
  textResponse: 'Minha resposta',
  status: 'SUBMITTED',
  grade: null,
  feedback: null,
  attachments: [],
  createdAt: '2026-09-10T10:00:00',
  updatedAt: null,
}

function renderDrawer() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const invalidate = vi.spyOn(queryClient, 'invalidateQueries')
  const onClose = vi.fn()

  render(
    <QueryClientProvider client={queryClient}>
      <SubmissionListDrawer open task={TASK} onClose={onClose} />
    </QueryClientProvider>,
  )

  return { invalidate, onClose }
}

async function openEvaluationDialog() {
  await waitFor(() => expect(screen.getByRole('button', { name: /avaliar/i })).toBeTruthy())
  await userEvent.click(screen.getByRole('button', { name: /avaliar/i }))
  await waitFor(() => expect(screen.getByPlaceholderText(/escreva o feedback/i)).toBeTruthy())
}

async function fillAndSave() {
  await userEvent.type(screen.getByPlaceholderText(/escreva o feedback/i), 'Bom trabalho')
  await userEvent.click(screen.getByRole('button', { name: /salvar avaliação/i }))
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(submissionsApi.listSubmissions).mockResolvedValue([SUBMISSION])
})

describe('SubmissionListDrawer', () => {
  it('closes the dialog once the evaluation is saved', async () => {
    vi.mocked(submissionsApi.evaluateSubmission).mockResolvedValue({
      ...SUBMISSION,
      status: 'EVALUATED',
      grade: 8.5,
      feedback: 'Bom trabalho',
      updatedAt: '2026-09-17T10:00:00',
    })

    renderDrawer()
    await openEvaluationDialog()
    await fillAndSave()

    await waitFor(() => expect(screen.queryByPlaceholderText(/escreva o feedback/i)).toBeNull())
  })

  it('invalidates the task submissions so the list refreshes without a reload', async () => {
    vi.mocked(submissionsApi.evaluateSubmission).mockResolvedValue({
      ...SUBMISSION,
      status: 'EVALUATED',
      grade: 8.5,
      feedback: 'Bom trabalho',
      updatedAt: '2026-09-17T10:00:00',
    })

    const { invalidate } = renderDrawer()
    await openEvaluationDialog()
    await fillAndSave()

    await waitFor(() =>
      expect(invalidate).toHaveBeenCalledWith({ queryKey: submissionKeys.byTask('task-1') }),
    )
  })

  it('keeps the dialog open when the API refuses, instead of losing what was typed', async () => {
    vi.mocked(submissionsApi.evaluateSubmission).mockRejectedValue({
      response: { status: 422, data: { error: 'SUBMISSION_ALREADY_EVALUATED' } },
    })

    renderDrawer()
    await openEvaluationDialog()
    await fillAndSave()

    await waitFor(() => expect(submissionsApi.evaluateSubmission).toHaveBeenCalled())
    expect(screen.getByPlaceholderText(/escreva o feedback/i)).toBeTruthy()
  })

  it('disables the submit button while the evaluation is in flight, so a second click cannot resend it', async () => {
    vi.mocked(submissionsApi.evaluateSubmission).mockImplementation(
      () => new Promise(() => {}),
    )

    renderDrawer()
    await openEvaluationDialog()
    await fillAndSave()

    await waitFor(() =>
      expect(
        screen.getByRole('button', { name: /salvar avaliação/i }).hasAttribute('disabled'),
      ).toBe(true),
    )
    expect(submissionsApi.evaluateSubmission).toHaveBeenCalledTimes(1)
  })

  // O drawer contava "N anexo(s)" e o diálogo mostrava só o nome: em nenhuma das
  // duas telas o professor conseguia abrir o arquivo que o aluno entregou.
  describe('submission attachments', () => {
    const COM_ANEXO: TaskSubmission = {
      ...SUBMISSION,
      attachments: [
        { id: 'att-1', fileKey: 'submission_attachment/2026/09/uuid-resposta.pdf', originalName: 'resposta.pdf', mimeType: 'application/pdf', sizeBytes: 4096 },
      ],
    }

    it('lists the attachment for download instead of counting it', async () => {
      vi.mocked(submissionsApi.listSubmissions).mockResolvedValue([COM_ANEXO])

      renderDrawer()

      expect(await screen.findByRole('button', { name: /baixar resposta\.pdf/i })).toBeTruthy()
      expect(screen.queryByText(/anexo\(s\)/i)).toBeNull()
    })

    it('offers it inside the evaluation dialog too', async () => {
      vi.mocked(submissionsApi.listSubmissions).mockResolvedValue([COM_ANEXO])

      renderDrawer()
      await openEvaluationDialog()

      const downloads = await screen.findAllByRole('button', { name: /baixar resposta\.pdf/i })
      expect(downloads.length).toBeGreaterThan(0)
    })
  })
})
