import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import TaskListPage from './TaskListPage'
import * as tasksApi from '../api/tasks'
import * as useSubjectListModule from '../hooks/useSubjectList'

vi.mock('../api/tasks')
vi.mock('../hooks/useSubjectList')

const draft = {
  id: 'task-1',
  subjectId: 'sub-1',
  organizationId: 'org-1',
  createdBy: 'prof-1',
  title: 'Lista 01',
  description: 'Enunciado',
  deadline: '2026-12-01T23:59:00',
  maxScore: null,
  status: 'DRAFT' as const,
  attachments: [],
  createdAt: '2026-09-01T10:00:00',
  updatedAt: null,
  submissionCount: 0,
  pendingEvaluationCount: 0,
}

const published = { ...draft, status: 'PUBLISHED' as const }
const closed = { ...draft, status: 'CLOSED' as const }
const comPendentes = { ...published, submissionCount: 5, pendingEvaluationCount: 3 }
const todasAvaliadas = { ...published, submissionCount: 4, pendingEvaluationCount: 0 }

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter>
        <TaskListPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(useSubjectListModule.useSubjectList).mockReturnValue(
    { data: [] } as unknown as ReturnType<typeof useSubjectListModule.useSubjectList>,
  )
})

describe('TaskListPage', () => {
  it('mostra a tarefa como publicada depois de publicar', async () => {
    const user = userEvent.setup()
    vi.mocked(tasksApi.listTasks)
      .mockResolvedValueOnce([draft])
      .mockResolvedValue([published])
    vi.mocked(tasksApi.publishTask).mockResolvedValue(published)

    renderPage()
    await user.click(await screen.findByRole('button', { name: /publicar/i }))

    await waitFor(() => {
      expect(screen.getByText('Publicada')).toBeTruthy()
    })
    expect(screen.queryByRole('button', { name: /publicar/i })).toBeNull()
  })

  it('mostra "Encerrada" quando o prazo já venceu', async () => {
    vi.mocked(tasksApi.listTasks).mockResolvedValue([closed])

    renderPage()

    expect(await screen.findByText('Encerrada')).toBeTruthy()
    expect(screen.queryByText('CLOSED')).toBeNull()
  })

  it('mantém "Ver Submissões" na tarefa encerrada, para o professor avaliar', async () => {
    vi.mocked(tasksApi.listTasks).mockResolvedValue([closed])

    renderPage()

    expect(await screen.findByRole('button', { name: /ver submissões/i })).toBeTruthy()
    expect(screen.queryByRole('button', { name: /publicar/i })).toBeNull()
  })

  it('traduz o rascunho em vez de mostrar o enum', async () => {
    vi.mocked(tasksApi.listTasks).mockResolvedValue([draft])

    renderPage()

    expect(await screen.findByText('Rascunho')).toBeTruthy()
  })

  it('destaca quantas respostas faltam avaliar', async () => {
    vi.mocked(tasksApi.listTasks).mockResolvedValue([comPendentes])

    renderPage()

    expect(await screen.findByText('3 a avaliar')).toBeTruthy()
    expect(screen.getByText('5 respostas')).toBeTruthy()
  })

  it('mostra o total sem badge quando não há pendência', async () => {
    vi.mocked(tasksApi.listTasks).mockResolvedValue([todasAvaliadas])

    renderPage()

    expect(await screen.findByText('4 respostas')).toBeTruthy()
    expect(screen.queryByText(/a avaliar/)).toBeNull()
  })

  it('não mostra contador algum na tarefa sem respostas', async () => {
    vi.mocked(tasksApi.listTasks).mockResolvedValue([published])

    renderPage()

    await screen.findByText('Lista 01')
    expect(screen.queryByText(/respostas?$/)).toBeNull()
    expect(screen.queryByText(/a avaliar/)).toBeNull()
  })

  it('usa o singular com uma resposta só', async () => {
    vi.mocked(tasksApi.listTasks).mockResolvedValue([
      { ...published, submissionCount: 1, pendingEvaluationCount: 1 },
    ])

    renderPage()

    expect(await screen.findByText('1 resposta')).toBeTruthy()
    expect(screen.getByText('1 a avaliar')).toBeTruthy()
  })

  it('mostra mensagem de erro quando a publicação falha', async () => {
    const user = userEvent.setup()
    vi.mocked(tasksApi.listTasks).mockResolvedValue([draft])
    vi.mocked(tasksApi.publishTask).mockRejectedValue({ response: { status: 409 } })

    renderPage()
    await user.click(await screen.findByRole('button', { name: /publicar/i }))

    await waitFor(() => {
      expect(screen.getByText(/já está publicada/i)).toBeTruthy()
    })
  })

  it('recarrega a lista mesmo quando a publicação falha', async () => {
    const user = userEvent.setup()
    vi.mocked(tasksApi.listTasks).mockResolvedValue([draft])
    vi.mocked(tasksApi.publishTask).mockRejectedValue({ response: { status: 409 } })

    renderPage()
    await screen.findByRole('button', { name: /publicar/i })
    expect(vi.mocked(tasksApi.listTasks)).toHaveBeenCalledTimes(1)

    await user.click(screen.getByRole('button', { name: /publicar/i }))

    await waitFor(() => {
      expect(vi.mocked(tasksApi.listTasks).mock.calls.length).toBeGreaterThan(1)
    })
  })

  describe('criação recusada', () => {
    async function tryToCreate() {
      const user = userEvent.setup()
      vi.mocked(useSubjectListModule.useSubjectList).mockReturnValue(
        { data: [{ id: 'sub-1', name: 'Matemática' }] } as unknown as ReturnType<typeof useSubjectListModule.useSubjectList>,
      )
      vi.mocked(tasksApi.listTasks).mockResolvedValue([])

      renderPage()
      await user.selectOptions(screen.getByRole('combobox'), 'sub-1')
      await user.click(screen.getByRole('button', { name: /nova tarefa/i }))
      await user.type(screen.getByPlaceholderText(/lista de exercícios/i), 'Lista 02')
      await user.type(screen.getByPlaceholderText(/markdown/i), 'Enunciado')
      await user.type(screen.getByLabelText(/data do prazo/i), '2030-01-01')
      await user.clear(screen.getByLabelText(/hora do prazo/i))
      await user.type(screen.getByLabelText(/hora do prazo/i), '10:00')
      await user.click(screen.getByRole('button', { name: /criar tarefa/i }))
      return user
    }

    it('explica a falta de vínculo e mantém o formulário preenchido', async () => {
      vi.mocked(tasksApi.createTask).mockRejectedValue({ response: { status: 403, data: { error: 'TASK_FORBIDDEN' } } })

      await tryToCreate()

      expect((await screen.findByRole('alert')).textContent).toMatch(/não leciona esta disciplina/i)
      expect((screen.getByPlaceholderText(/lista de exercícios/i) as HTMLInputElement).value).toBe('Lista 02')
    })

    it('mostra as demais recusas com a mensagem da API', async () => {
      vi.mocked(tasksApi.createTask).mockRejectedValue({ response: { status: 422, data: { error: 'DEADLINE_NOT_IN_FUTURE' } } })

      await tryToCreate()

      expect((await screen.findByRole('alert')).textContent).toMatch(/data futura/i)
    })

    it('não herda a recusa anterior ao reabrir o diálogo', async () => {
      vi.mocked(tasksApi.createTask).mockRejectedValue({ response: { status: 403, data: { error: 'TASK_FORBIDDEN' } } })

      const user = await tryToCreate()
      await screen.findByRole('alert')
      await user.click(screen.getByRole('button', { name: /cancelar/i }))
      await user.click(screen.getByRole('button', { name: /nova tarefa/i }))

      expect(screen.queryByRole('alert')).toBeNull()
    })
  })
})
