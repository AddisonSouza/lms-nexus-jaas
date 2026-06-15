import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import TopicFormDialog from './TopicFormDialog'

describe('TopicFormDialog', () => {
  it('renders title field and submit button when open', () => {
    render(
      <TopicFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        title="Novo Tópico"
      />,
    )
    expect(screen.getByPlaceholderText(/introdução/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /salvar/i })).toBeTruthy()
  })

  it('shows validation error for empty title on submit', async () => {
    render(
      <TopicFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        title="Novo Tópico"
      />,
    )
    await userEvent.click(screen.getByRole('button', { name: /salvar/i }))
    await waitFor(() => {
      expect(screen.getByText(/ao menos 2 caracteres/i)).toBeTruthy()
    })
  })

  it('calls onSubmit with title when valid', async () => {
    const onSubmit = vi.fn()
    render(
      <TopicFormDialog
        open
        onClose={vi.fn()}
        onSubmit={onSubmit}
        isPending={false}
        title="Novo Tópico"
      />,
    )
    await userEvent.type(screen.getByPlaceholderText(/introdução/i), 'Aula 1')
    await userEvent.click(screen.getByRole('button', { name: /salvar/i }))
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({ title: 'Aula 1' }),
        expect.anything(),
      )
    })
  })

  it('disables submit button when isPending', () => {
    render(
      <TopicFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending
        title="Novo Tópico"
      />,
    )
    expect(screen.getByRole('button', { name: /salvar/i }).hasAttribute('disabled')).toBe(true)
  })

  it('returns null when closed', () => {
    const { container } = render(
      <TopicFormDialog
        open={false}
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        title="Novo Tópico"
      />,
    )
    expect(container.firstChild).toBeNull()
  })

  it('pre-fills title from defaultValues', () => {
    render(
      <TopicFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        defaultValues={{ title: 'Tópico existente' }}
        title="Editar Tópico"
      />,
    )
    expect((screen.getByPlaceholderText(/introdução/i) as HTMLInputElement).value).toBe('Tópico existente')
  })
})
