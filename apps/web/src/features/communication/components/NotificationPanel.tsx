import { CheckCheck } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useNotifications } from '../hooks/useNotifications'
import { useMarkNotificationRead, useMarkAllNotificationsRead } from '../hooks/useNotificationMutations'
import type { Notification } from '../types'

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
    <div className="flex max-h-96 flex-col">
      <div className="flex items-center justify-between border-b px-3 py-2">
        <span className="text-sm font-semibold">Notificações</span>
        {unreadCount > 0 && (
          <button
            onClick={handleMarkAllRead}
            disabled={markAllRead.isPending}
            className="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground disabled:opacity-50"
          >
            <CheckCheck className="h-3.5 w-3.5" />
            Marcar todas como lidas
          </button>
        )}
      </div>

      <div className="flex-1 overflow-y-auto">
        {isLoading && <p className="px-3 py-4 text-sm text-muted-foreground">Carregando notificações...</p>}

        {!isLoading && notifications.length === 0 && (
          <p className="px-3 py-4 text-sm text-muted-foreground">Nenhuma notificação.</p>
        )}

        <ul>
          {notifications.map((notification) => (
            <li key={notification.id}>
              <button
                onClick={() => handleSelect(notification)}
                className="flex w-full flex-col gap-0.5 border-b px-3 py-2 text-left hover:bg-muted"
              >
                <div className="flex items-center gap-1.5">
                  {!notification.read && <span className="h-1.5 w-1.5 shrink-0 rounded-full bg-primary" />}
                  <span className={notification.read ? 'text-sm' : 'text-sm font-semibold'}>
                    {notification.title}
                  </span>
                </div>
                <p className="text-xs text-muted-foreground">{notification.message}</p>
                <p className="text-[11px] text-muted-foreground">
                  {new Date(notification.createdAt).toLocaleString('pt-BR')}
                </p>
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}

export default NotificationPanel
