import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import ConfirmDialog from './ConfirmDialog'

const base = {
  open: true,
  title: 'Excluir disciplina',
  description: 'Confirmar exclusão?',
  onConfirm: vi.fn(),
  onCancel: vi.fn(),
}

describe('ConfirmDialog', () => {
  it('shows no alert while there is no error', () => {
    render(<ConfirmDialog {...base} />)

    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('shows the refusal without closing the dialog', () => {
    render(<ConfirmDialog {...base} error="Você não tem permissão para esta ação." />)

    expect(screen.getByRole('alert').textContent).toBe('Você não tem permissão para esta ação.')
    expect(screen.getByText('Confirmar exclusão?')).toBeTruthy()
  })
})
