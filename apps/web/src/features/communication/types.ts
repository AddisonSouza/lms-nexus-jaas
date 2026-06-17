export interface AnnouncementAttachment {
  id: string
  fileKey: string | null
  originalName: string | null
  mimeType: string | null
  sizeBytes: number | null
  externalUrl: string | null
  linkTitle: string | null
}

export interface Announcement {
  id: string
  classroomId: string
  organizationId: string
  authorId: string
  content: string
  attachments: AnnouncementAttachment[]
  createdAt: string
  updatedAt: string | null
}

export interface CreateAnnouncementPayload {
  classroomId: string
  content: string
  externalUrl?: string
  linkTitle?: string
  files?: File[]
}

export interface UpdateAnnouncementPayload {
  id: string
  content?: string
  externalUrl?: string
  linkTitle?: string
  files?: File[]
}

export type NotificationType =
  | 'ANNOUNCEMENT_POSTED'
  | 'TASK_PUBLISHED'
  | 'TASK_SUBMITTED'
  | 'SUBMISSION_EVALUATED'

export interface Notification {
  id: string
  type: NotificationType
  referenceId: string
  title: string
  message: string
  actionLink: string
  read: boolean
  createdAt: string
}

export interface NotificationListResponse {
  items: Notification[]
  unreadCount: number
}
