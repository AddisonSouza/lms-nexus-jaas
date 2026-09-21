import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import AnnouncementFeed from './AnnouncementFeed'
import * as announcementsApi from '../api/announcements'
import type { Announcement } from '../types'

vi.mock('../api/announcements')

let mockRole: string | null = 'PROFESSOR'
let mockUserId: string | null = 'prof-1'

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: mockRole, userId: mockUserId })),
}))

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>
}

const ANNOUNCEMENT: Announcement = {
  id: 'ann-1',
  classroomId: 'class-1',
  organizationId: 'org-1',
  authorId: 'prof-1',
  content: 'Prova na próxima semana',
  attachments: [],
  createdAt: '2026-01-01T10:00:00',
  updatedAt: null,
}

// Associação e permissão de publicar chegam da rota, que é quem pode consultar
// os membros da turma. O padrão aqui é o professor membro daquela turma.
function renderFeed(props: { isMember?: boolean; canPost?: boolean } = {}) {
  const { isMember = true, canPost = true } = props
  return render(
    <AnnouncementFeed classroomId="class-1" isMember={isMember} canPost={canPost} />,
    { wrapper },
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  mockRole = 'PROFESSOR'
  mockUserId = 'prof-1'
})

describe('AnnouncementFeed', () => {
  it('shows empty state when there are no announcements', async () => {
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([])
    renderFeed()
    await waitFor(() => {
      expect(screen.getByText(/nenhum aviso publicado/i)).toBeTruthy()
    })
  })

  it('renders the ordered list of announcements returned by the API', async () => {
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([ANNOUNCEMENT])
    renderFeed()
    await waitFor(() => {
      expect(screen.getByText('Prova na próxima semana')).toBeTruthy()
    })
  })

  it('shows the "Novo Aviso" action for a PROFESSOR', async () => {
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([])
    renderFeed()
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /novo aviso/i })).toBeTruthy()
    })
  })

  // Publicar exige papel PROFESSOR **na turma**. Gestor e admin têm papel largo
  // na organização mas levam 403 aqui, então não recebem `canPost`.
  it('hides the "Novo Aviso" action from a member who does not teach the class', async () => {
    mockRole = 'GESTOR'
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([])
    renderFeed({ canPost: false })
    await waitFor(() => {
      expect(screen.getByText(/nenhum aviso publicado/i)).toBeTruthy()
    })
    expect(screen.queryByRole('button', { name: /novo aviso/i })).toBeNull()
  })

  it('hides the "Novo Aviso" action for an ALUNO', async () => {
    mockRole = 'ALUNO'
    mockUserId = 'student-1'
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([])
    renderFeed({ canPost: false })
    await waitFor(() => {
      expect(screen.queryByRole('button', { name: /novo aviso/i })).toBeNull()
    })
  })

  // `ListAnnouncementsService` exige associação à turma seja qual for o papel:
  // admin e gestor levam 403. Em vez de um mural quebrado, ele some.
  it('renders nothing for someone who is not a member of the class', async () => {
    mockRole = 'ADMIN_ORG'
    mockUserId = 'admin-1'
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([ANNOUNCEMENT])

    const { container } = renderFeed({ isMember: false, canPost: false })

    await waitFor(() => {
      expect(container.firstChild).toBeNull()
    })
    expect(announcementsApi.listAnnouncements).not.toHaveBeenCalled()
  })

  it('shows edit/delete actions only for the announcement author', async () => {
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([ANNOUNCEMENT])
    renderFeed()
    await waitFor(() => {
      expect(screen.getByTitle(/editar/i)).toBeTruthy()
      expect(screen.getByTitle(/excluir/i)).toBeTruthy()
    })
  })

  it('hides edit/delete actions for a non-author professor', async () => {
    mockUserId = 'other-prof'
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([ANNOUNCEMENT])
    renderFeed()
    await waitFor(() => {
      expect(screen.getByText('Prova na próxima semana')).toBeTruthy()
    })
    expect(screen.queryByTitle(/editar/i)).toBeNull()
    expect(screen.queryByTitle(/excluir/i)).toBeNull()
  })

  it('submits a new announcement through the form', async () => {
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([])
    vi.mocked(announcementsApi.createAnnouncement).mockResolvedValue(ANNOUNCEMENT)

    renderFeed()
    await userEvent.click(await screen.findByRole('button', { name: /novo aviso/i }))
    await userEvent.type(screen.getByPlaceholderText(/escreva o aviso/i), 'Prova na próxima semana')
    await userEvent.click(screen.getByRole('button', { name: /^publicar$/i }))

    await waitFor(() => {
      expect(announcementsApi.createAnnouncement).toHaveBeenCalledWith(
        expect.objectContaining({ classroomId: 'class-1', content: 'Prova na próxima semana' }),
        expect.anything(),
      )
    })
  })

  it('keeps the form open showing why the API refused to publish', async () => {
    vi.mocked(announcementsApi.listAnnouncements).mockResolvedValue([])
    vi.mocked(announcementsApi.createAnnouncement).mockRejectedValue({
      response: { status: 403, data: { error: 'Forbidden' } },
    })

    renderFeed()
    await userEvent.click(await screen.findByRole('button', { name: /novo aviso/i }))
    await userEvent.type(screen.getByPlaceholderText(/escreva o aviso/i), 'Prova na próxima semana')
    await userEvent.click(screen.getByRole('button', { name: /^publicar$/i }))

    const alert = await screen.findByRole('alert')
    expect(alert.textContent).toBe('Você não tem permissão para esta ação.')
    expect(screen.getByPlaceholderText(/escreva o aviso/i)).toBeTruthy()
  })
})
