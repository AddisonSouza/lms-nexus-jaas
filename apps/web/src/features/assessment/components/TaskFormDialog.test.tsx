import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import TaskFormDialog from './TaskFormDialog'

const BASE_PROPS = {
  open: true,
  subjectId: 'sub-1',
  onClose: vi.fn(),
  onSubmit: vi.fn(),
  isPending: false,
}

describe('TaskFormDialog', () => {
  it('renders all fields when open', () => {
    render(<TaskFormDialog {...BASE_PROPS} />)
    expect(screen.getByPlaceholderText(/lista de exercícios/i)).toBeTruthy()
    expect(screen.getByPlaceholderText(/markdown/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /criar tarefa/i })).toBeTruthy()
  })

  it('returns null when closed', () => {
    const { container } = render(<TaskFormDialog {...BASE_PROPS} open={false} />)
    expect(container.firstChild).toBeNull()
  })

  it('shows validation error for empty title on submit', async () => {
    render(<TaskFormDialog {...BASE_PROPS} />)
    await userEvent.click(screen.getByRole('button', { name: /criar tarefa/i }))
    await waitFor(() => {
      expect(screen.getByText(/título é obrigatório/i)).toBeTruthy()
    })
  })

  it('shows validation error for past deadline', async () => {
    render(<TaskFormDialog {...BASE_PROPS} />)
    await userEvent.type(screen.getByPlaceholderText(/lista de exercícios/i), 'Tarefa X')
    await userEvent.type(screen.getByPlaceholderText(/markdown/i), 'Enunciado')

    const deadlineInput = screen.getByLabelText(/prazo/i)
    await userEvent.type(deadlineInput, '2020-01-01T10:00')

    await userEvent.click(screen.getByRole('button', { name: /criar tarefa/i }))
    await waitFor(() => {
      expect(screen.getByText(/data futura/i)).toBeTruthy()
    })
  })

  it('disables submit button when isPending', () => {
    render(<TaskFormDialog {...BASE_PROPS} isPending />)
    expect(screen.getByRole('button', { name: /criar tarefa/i }).hasAttribute('disabled')).toBe(true)
  })
})
