import { useState } from 'react'
import { Bell } from 'lucide-react'
import { Popover, PopoverTrigger, PopoverContent } from '@components/ui/popover'
import { useNotifications } from '../hooks/useNotifications'
import NotificationPanel from './NotificationPanel'

function NotificationBell() {
  const [open, setOpen] = useState(false)
  const { data } = useNotifications()
  const unreadCount = data?.unreadCount ?? 0

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        className="relative flex h-8 w-8 items-center justify-center rounded-full hover:bg-muted"
        aria-label="Notificações"
      >
        <Bell className="h-4 w-4" />
        {unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-destructive px-1 text-[10px] font-medium leading-none text-destructive-foreground">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </PopoverTrigger>
      <PopoverContent>
        <NotificationPanel onClose={() => setOpen(false)} />
      </PopoverContent>
    </Popover>
  )
}

export default NotificationBell
