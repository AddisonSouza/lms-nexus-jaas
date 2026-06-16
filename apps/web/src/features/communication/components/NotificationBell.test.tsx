import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import NotificationBell from './NotificationBell'
import * as notificationsApi from '../api/notifications'
import type { Notification, NotificationListResponse } from '../types'

vi.mock('../api/notifications')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>
}

const UNREAD_NOTIFICATION: Notification = {
  id: 'notif-1',
  type: 'ANNOUNCEMENT_POSTED',
  referenceId: 'ann-1',
  title: 'Novo aviso',
  message: 'Prova na próxima semana',
  actionLink: '/classrooms/class-1',
  read: false,
  createdAt: '2026-01-01T10:00:00',
}

const READ_NOTIFICATION: Notification = {
  id: 'notif-2',
  type: 'TASK_PUBLISHED',
  referenceId: 'task-1',
  title: 'Nova tarefa',
  message: 'Tarefa publicada',
  actionLink: '/assessment/student-tasks?taskId=task-1',
  read: true,
  createdAt: '2026-01-01T09:00:00',
}

function listResponse(items: Notification[], unreadCount: number): NotificationListResponse {
  return { items, unreadCount }
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('NotificationBell', () => {
  it('shows unread count badge when there are unread notifications', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(
      listResponse([UNREAD_NOTIFICATION, READ_NOTIFICATION], 1),
    )

    render(<NotificationBell />, { wrapper })

    await waitFor(() => {
      expect(screen.getByText('1')).toBeTruthy()
    })
  })

  it('caps the displayed badge count at "9+"', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(listResponse([], 12))

    render(<NotificationBell />, { wrapper })

    await waitFor(() => {
      expect(screen.getByText('9+')).toBeTruthy()
    })
  })

  it('hides the badge when there are no unread notifications', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(listResponse([READ_NOTIFICATION], 0))

    render(<NotificationBell />, { wrapper })

    await waitFor(() => {
      expect(notificationsApi.listNotifications).toHaveBeenCalled()
    })

    expect(screen.queryByText('0')).toBeNull()
  })
})

describe('NotificationPanel', () => {
  it('shows the empty state when there are no notifications', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(listResponse([], 0))

    render(<NotificationBell />, { wrapper })
    await userEvent.click(screen.getByLabelText(/notificações/i))

    await waitFor(() => {
      expect(screen.getByText(/nenhuma notificação/i)).toBeTruthy()
    })
  })

  it('renders notification items with title and message', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(
      listResponse([UNREAD_NOTIFICATION], 1),
    )

    render(<NotificationBell />, { wrapper })
    await userEvent.click(screen.getByLabelText(/notificações/i))

    await waitFor(() => {
      expect(screen.getByText('Novo aviso')).toBeTruthy()
      expect(screen.getByText('Prova na próxima semana')).toBeTruthy()
    })
  })

  it('marks a notification as read and navigates to its actionLink on click', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(
      listResponse([UNREAD_NOTIFICATION], 1),
    )
    vi.mocked(notificationsApi.markNotificationRead).mockResolvedValue(undefined)

    render(<NotificationBell />, { wrapper })
    await userEvent.click(screen.getByLabelText(/notificações/i))

    await waitFor(() => screen.getByText('Novo aviso'))
    await userEvent.click(screen.getByText('Novo aviso'))

    await waitFor(() => {
      expect(notificationsApi.markNotificationRead).toHaveBeenCalledWith('notif-1', expect.anything())
      expect(mockNavigate).toHaveBeenCalledWith('/classrooms/class-1')
    })
  })

  it('does not call markNotificationRead when clicking an already-read notification', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(
      listResponse([READ_NOTIFICATION], 0),
    )

    render(<NotificationBell />, { wrapper })
    await userEvent.click(screen.getByLabelText(/notificações/i))

    await waitFor(() => screen.getByText('Nova tarefa'))
    await userEvent.click(screen.getByText('Nova tarefa'))

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/assessment/student-tasks?taskId=task-1')
    })
    expect(notificationsApi.markNotificationRead).not.toHaveBeenCalled()
  })

  it('calls markAllNotificationsRead when "Marcar todas como lidas" is clicked', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(
      listResponse([UNREAD_NOTIFICATION], 1),
    )
    vi.mocked(notificationsApi.markAllNotificationsRead).mockResolvedValue({ unreadCount: 0 })

    render(<NotificationBell />, { wrapper })
    await userEvent.click(screen.getByLabelText(/notificações/i))

    await waitFor(() => screen.getByRole('button', { name: /marcar todas como lidas/i }))
    await userEvent.click(screen.getByRole('button', { name: /marcar todas como lidas/i }))

    await waitFor(() => {
      expect(notificationsApi.markAllNotificationsRead).toHaveBeenCalled()
    })
  })

  it('does not show "Marcar todas como lidas" when there are no unread notifications', async () => {
    vi.mocked(notificationsApi.listNotifications).mockResolvedValue(listResponse([READ_NOTIFICATION], 0))

    render(<NotificationBell />, { wrapper })
    await userEvent.click(screen.getByLabelText(/notificações/i))

    await waitFor(() => screen.getByText('Nova tarefa'))
    expect(screen.queryByRole('button', { name: /marcar todas como lidas/i })).toBeNull()
  })
})
