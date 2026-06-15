import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import SubjectFormDialog from './SubjectFormDialog'

describe('SubjectFormDialog', () => {
  it('renders name field and submit button when open', () => {
    render(
      <SubjectFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        title="Nova Disciplina"
      />,
    )
    expect(screen.getByPlaceholderText(/matemática/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /salvar/i })).toBeTruthy()
  })

  it('shows validation error for empty name on submit', async () => {
    render(
      <SubjectFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        title="Nova Disciplina"
      />,
    )
    await userEvent.click(screen.getByRole('button', { name: /salvar/i }))
    await waitFor(() => {
      expect(screen.getByText(/ao menos 2 caracteres/i)).toBeTruthy()
    })
  })

  it('calls onSubmit with form data when valid', async () => {
    const onSubmit = vi.fn()
    render(
      <SubjectFormDialog
        open
        onClose={vi.fn()}
        onSubmit={onSubmit}
        isPending={false}
        title="Nova Disciplina"
      />,
    )
    await userEvent.type(screen.getByPlaceholderText(/matemática/i), 'Física')
    await userEvent.click(screen.getByRole('button', { name: /salvar/i }))
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({ name: 'Física' }),
        expect.anything(),
      )
    })
  })

  it('shows spinner and disables button when isPending', () => {
    render(
      <SubjectFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending
        title="Nova Disciplina"
      />,
    )
    expect(screen.getByRole('button', { name: /salvar/i }).hasAttribute('disabled')).toBe(true)
  })

  it('returns null when closed', () => {
    const { container } = render(
      <SubjectFormDialog
        open={false}
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        title="Nova Disciplina"
      />,
    )
    expect(container.firstChild).toBeNull()
  })
})
