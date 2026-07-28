import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import ContentFormDialog from './ContentFormDialog'
import type { Topic } from '../types'

const TOPICS: Topic[] = [
  {
    id: 't1',
    subjectId: 's1',
    organizationId: 'o1',
    title: 'Tópico 1',
    position: 0,
    createdAt: '',
    updatedAt: null,
  },
]

describe('ContentFormDialog', () => {
  it('renders title field when open', () => {
    render(
      <ContentFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        topics={TOPICS}
        title="Novo Conteúdo"
      />,
    )
    expect(screen.getByPlaceholderText(/aula 1/i)).toBeTruthy()
  })

  it('shows URL field for LINK type by default', () => {
    render(
      <ContentFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        topics={TOPICS}
        title="Novo Conteúdo"
      />,
    )
    expect(screen.getByPlaceholderText(/https/i)).toBeTruthy()
  })

  it('shows file input when DOCUMENTO type is selected', async () => {
    render(
      <ContentFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        topics={TOPICS}
        title="Novo Conteúdo"
      />,
    )
    await userEvent.click(screen.getByRole('radio', { name: /documento/i }))
    await waitFor(() => {
      expect(screen.queryByPlaceholderText(/https/i)).toBeNull()
      expect(document.querySelector('input[type="file"]')).toBeTruthy()
    })
  })

  it('shows validation error when submitting empty title', async () => {
    render(
      <ContentFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        topics={TOPICS}
        title="Novo Conteúdo"
      />,
    )
    await userEvent.click(screen.getByRole('button', { name: /salvar/i }))
    await waitFor(() => {
      expect(screen.getByText(/ao menos 2 caracteres/i)).toBeTruthy()
    })
  })

  it('returns null when closed', () => {
    const { container } = render(
      <ContentFormDialog
        open={false}
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending={false}
        topics={TOPICS}
        title="Novo Conteúdo"
      />,
    )
    expect(container.firstChild).toBeNull()
  })

  it('disables submit when isPending', () => {
    render(
      <ContentFormDialog
        open
        onClose={vi.fn()}
        onSubmit={vi.fn()}
        isPending
        topics={TOPICS}
        title="Novo Conteúdo"
      />,
    )
    expect(screen.getByRole('button', { name: /salvar/i }).hasAttribute('disabled')).toBe(true)
  })
})
