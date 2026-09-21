import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ClassroomDetailPage from './ClassroomDetailPage'

let mockRole: string | null = 'ADMIN_ORG'

vi.mock('@store/authStore', () => ({
  useAuthStore: vi.fn((selector) => selector({ role: mockRole, userId: 'u-1', organizationId: 'org-1' })),
}))

vi.mock('../hooks/useClassroom', () => ({
  useClassroom: () => ({
    data: {
      id: 'class-1',
      name: 'Turma A',
      academicPeriod: '2026/2',
      status: 'ACTIVE',
      inviteCode: 'ABC123',
      description: null,
    },
    isLoading: false,
  }),
}))
vi.mock('../hooks/useUpdateClassroom', () => ({
  useUpdateClassroom: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('../hooks/useDeleteClassroom', () => ({
  useDeleteClassroom: () => ({ mutate: vi.fn(), isPending: false }),
}))
vi.mock('./ClassroomMembersPanel', () => ({
  default: () => <div>membros</div>,
}))

beforeEach(() => {
  vi.clearAllMocks()
  mockRole = 'ADMIN_ORG'
})

function renderPage(announcementFeedSlot?: React.ReactNode) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/classrooms/class-1']}>
        <Routes>
          <Route
            path="/classrooms/:id"
            element={<ClassroomDetailPage announcementFeedSlot={announcementFeedSlot} />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

// `ListAnnouncementsService` exige associação à turma seja qual for o papel na
// organização. A rota entrega o slot só a quem é membro; sem ele o título
// sozinho anunciava um mural que nunca chegava.
describe('ClassroomDetailPage — Mural de Avisos', () => {
  it('shows the board when the route hands it a feed', () => {
    renderPage(<div>mural</div>)

    expect(screen.getByRole('heading', { name: /mural de avisos/i })).toBeTruthy()
    expect(screen.getByText('mural')).toBeTruthy()
  })

  it('hides the heading too when there is no feed to show', () => {
    renderPage(null)

    expect(screen.queryByRole('heading', { name: /mural de avisos/i })).toBeNull()
  })
})
