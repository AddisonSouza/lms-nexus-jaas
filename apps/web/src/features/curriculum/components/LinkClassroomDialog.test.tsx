import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import LinkClassroomDialog from './LinkClassroomDialog'
import * as orgClassroomApi from '../api/org-classroom-api'

vi.mock('../api/org-classroom-api')

const CLASSROOMS = [
  { id: 'c-1', name: 'Turma Ativa', status: 'ACTIVE' as const },
  { id: 'c-2', name: 'Turma Vinculada', status: 'ACTIVE' as const },
  { id: 'c-3', name: 'Turma Arquivada', status: 'ARCHIVED' as const },
]

function renderDialog(props: Partial<React.ComponentProps<typeof LinkClassroomDialog>> = {}) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <LinkClassroomDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        linkedClassroomIds={[]}
        {...props}
      />
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.mocked(orgClassroomApi.listOrgClassrooms).mockResolvedValue(CLASSROOMS)
})

describe('LinkClassroomDialog', () => {
  it('lists only active classrooms that are not linked yet', async () => {
    renderDialog({ linkedClassroomIds: ['c-2'] })

    await waitFor(() => expect(screen.getByRole('option', { name: 'Turma Ativa' })).toBeTruthy())
    expect(screen.queryByRole('option', { name: 'Turma Vinculada' })).toBeNull()
    expect(screen.queryByRole('option', { name: 'Turma Arquivada' })).toBeNull()
  })

  it('submits the chosen classroom id', async () => {
    const onSubmit = vi.fn()
    renderDialog({ onSubmit })

    await waitFor(() => expect(screen.getByRole('option', { name: 'Turma Ativa' })).toBeTruthy())
    await userEvent.selectOptions(screen.getByLabelText(/turma \*/i), 'c-1')
    await userEvent.click(screen.getByRole('button', { name: /vincular/i }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith('c-1'))
  })

  it('does not submit without a choice', async () => {
    const onSubmit = vi.fn()
    renderDialog({ onSubmit })

    await waitFor(() => expect(screen.getByRole('option', { name: 'Turma Ativa' })).toBeTruthy())
    await userEvent.click(screen.getByRole('button', { name: /vincular/i }))

    await waitFor(() => expect(screen.getByText('Escolha uma turma')).toBeTruthy())
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('shows the API refusal without closing', async () => {
    renderDialog({ error: 'Esta turma está arquivada.' })

    await waitFor(() => expect(screen.getByText('Esta turma está arquivada.')).toBeTruthy())
  })

  it('explains when there is no classroom left to link', async () => {
    renderDialog({ linkedClassroomIds: ['c-1', 'c-2'] })

    await waitFor(() =>
      expect(screen.getByText(/nenhuma turma ativa disponível/i)).toBeTruthy(),
    )
    expect(screen.getByRole('button', { name: /vincular/i }).hasAttribute('disabled')).toBe(true)
  })
})
