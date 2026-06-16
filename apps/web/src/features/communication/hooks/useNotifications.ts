import { useQuery } from '@tanstack/react-query'
import { listNotifications } from '../api/notifications'
import { notificationKeys } from '../api/query-keys'

export function useNotifications() {
  return useQuery({
    queryKey: notificationKeys.all,
    queryFn: listNotifications,
    refetchInterval: 30_000,
  })
}
