import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import SubmissionFormDialog from './SubmissionFormDialog'

const FUTURE = new Date(Date.now() + 86_400_000).toISOString()
const PAST = new Date(Date.now() - 86_400_000).toISOString()

function renderDialog(props: Partial<React.ComponentProps<typeof SubmissionFormDialog>> = {}) {
  return render(
    <SubmissionFormDialog
      open
      taskTitle="Lista de exercícios"
      deadline={FUTURE}
      onClose={vi.fn()}
      onSubmit={vi.fn()}
      isPending={false}
      {...props}
    />,
  )
}

describe('SubmissionFormDialog', () => {
  it('shows the API refusal so a rejected file does not look like a silent failure', () => {
    renderDialog({ error: 'Este tipo de arquivo não é permitido.' })

    expect(screen.getByRole('alert').textContent).toBe('Este tipo de arquivo não é permitido.')
  })

  it('stays quiet when nothing failed', () => {
    renderDialog()

    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('keeps the form open after a refusal, so the student can pick another file', () => {
    renderDialog({ error: 'Este tipo de arquivo não é permitido.' })

    expect(screen.getByRole('button', { name: /enviar resposta/i })).toBeTruthy()
  })

  it('offers no form at all once the deadline is gone', () => {
    renderDialog({ deadline: PAST })

    expect(screen.getByText(/o prazo desta tarefa expirou/i)).toBeTruthy()
    expect(screen.queryByRole('button', { name: /enviar resposta/i })).toBeNull()
  })
})
