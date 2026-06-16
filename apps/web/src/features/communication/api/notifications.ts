import { z } from 'zod'
import api from '@lib/axios'

const notificationSchema = z.object({
  id: z.string(),
  type: z.enum(['ANNOUNCEMENT_POSTED', 'TASK_PUBLISHED', 'TASK_SUBMITTED', 'SUBMISSION_EVALUATED']),
  referenceId: z.string(),
  title: z.string(),
  message: z.string(),
  actionLink: z.string(),
  read: z.boolean(),
  createdAt: z.string(),
})

const notificationListSchema = z.object({
  items: z.array(notificationSchema),
  unreadCount: z.number(),
})

export async function listNotifications() {
  const res = await api.get('/notifications')
  return notificationListSchema.parse(res.data)
}

export async function markNotificationRead(id: string) {
  await api.patch(`/notifications/${id}/read`)
}

export async function markAllNotificationsRead() {
  const res = await api.patch('/notifications/read-all')
  return z.object({ unreadCount: z.number() }).parse(res.data)
}
