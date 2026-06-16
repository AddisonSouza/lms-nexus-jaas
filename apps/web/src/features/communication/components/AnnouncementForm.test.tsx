import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import AnnouncementForm from './AnnouncementForm'
import type { Announcement } from '../types'

const BASE_PROPS = {
  open: true,
  onClose: vi.fn(),
  onSubmit: vi.fn(),
  isPending: false,
}

const ANNOUNCEMENT: Announcement = {
  id: 'ann-1',
  classroomId: 'class-1',
  organizationId: 'org-1',
  authorId: 'prof-1',
  content: 'Aviso existente',
  attachments: [],
  createdAt: '2026-01-01T10:00:00',
  updatedAt: null,
}

describe('AnnouncementForm', () => {
  it('renders content field and publish button when creating', () => {
    render(<AnnouncementForm {...BASE_PROPS} />)
    expect(screen.getByPlaceholderText(/escreva o aviso/i)).toBeTruthy()
    expect(screen.getByRole('button', { name: /publicar/i })).toBeTruthy()
  })

  it('returns null when closed', () => {
    const { container } = render(<AnnouncementForm {...BASE_PROPS} open={false} />)
    expect(container.firstChild).toBeNull()
  })

  it('shows validation error for empty content on submit', async () => {
    render(<AnnouncementForm {...BASE_PROPS} />)
    await userEvent.click(screen.getByRole('button', { name: /publicar/i }))
    await waitFor(() => {
      expect(screen.getByText(/conteúdo do aviso é obrigatório/i)).toBeTruthy()
    })
  })

  it('disables submit button when isPending', () => {
    render(<AnnouncementForm {...BASE_PROPS} isPending />)
    expect(screen.getByRole('button', { name: /publicar/i }).hasAttribute('disabled')).toBe(true)
  })

  it('pre-fills content and shows save button when editing', () => {
    render(<AnnouncementForm {...BASE_PROPS} announcement={ANNOUNCEMENT} />)
    expect(screen.getByDisplayValue('Aviso existente')).toBeTruthy()
    expect(screen.getByRole('button', { name: /salvar/i })).toBeTruthy()
  })

  it('submits content typed by the user', async () => {
    const onSubmit = vi.fn()
    render(<AnnouncementForm {...BASE_PROPS} onSubmit={onSubmit} />)
    await userEvent.type(screen.getByPlaceholderText(/escreva o aviso/i), 'Prova na próxima semana')
    await userEvent.click(screen.getByRole('button', { name: /publicar/i }))
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({ content: 'Prova na próxima semana' }),
        expect.anything(),
      )
    })
  })
})
