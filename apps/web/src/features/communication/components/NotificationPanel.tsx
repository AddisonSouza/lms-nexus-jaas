import { useNavigate } from 'react-router-dom'
import { useNotifications } from '../hooks/useNotifications'
import { useMarkNotificationRead, useMarkAllNotificationsRead } from '../hooks/useNotificationMutations'
import type { Notification } from '../types'
import { Button } from '@components/ui/button'
import { CardKicker } from '@components/ui/card'

interface Props {
  onClose: () => void
}

function NotificationPanel({ onClose }: Props) {
  const navigate = useNavigate()
  const { data, isLoading } = useNotifications()
  const markRead = useMarkNotificationRead()
  const markAllRead = useMarkAllNotificationsRead()

  const notifications = data?.items ?? []
  const unreadCount = data?.unreadCount ?? 0

  function handleSelect(notification: Notification) {
    if (!notification.read) {
      markRead.mutate(notification.id)
    }
    onClose()
    navigate(notification.actionLink)
  }

  function handleMarkAllRead() {
    markAllRead.mutate()
  }

  return (
    <div className="flex max-h-96 flex-col gap-[var(--space-2)] p-[var(--space-2)]">
      <div className="flex items-center justify-between px-2 pt-1">
        <CardKicker>Notificações</CardKicker>
        {unreadCount > 0 && (
          <Button
            variant="ghost"
            size="sm"
            onClick={handleMarkAllRead}
            disabled={markAllRead.isPending}
            className="text-xs"
          >
            Marcar todas como lidas
          </Button>
        )}
      </div>

      <div className="flex-1 overflow-y-auto">
        {isLoading && <p className="px-2 py-4 text-sm text-muted-foreground">Carregando notificações...</p>}

        {!isLoading && notifications.length === 0 && (
          <p className="px-2 py-4 text-sm text-muted-foreground">Nenhuma notificação.</p>
        )}

        <ul className="flex flex-col gap-0.5">
          {notifications.map((notification) => (
            <li key={notification.id}>
              <button
                onClick={() => handleSelect(notification)}
                className="flex w-full gap-2.5 rounded-[var(--radius-md)] px-2 py-2 text-left hover:bg-muted"
              >
                <span
                  className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${notification.read ? 'bg-transparent' : 'bg-accent'}`}
                />
                <span className="flex-1">
                  <span className={notification.read ? 'block text-sm' : 'block text-sm font-semibold'}>
                    {notification.title}
                  </span>
                  <span className="block text-xs text-muted-foreground">{notification.message}</span>
                  <span className="mt-0.5 block text-[11px] text-muted-foreground">
                    {new Date(notification.createdAt).toLocaleString('pt-BR')}
                  </span>
                </span>
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}

export default NotificationPanel
