export type TaskStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'GRADED'

export interface TaskAttachment {
  id: string
  fileKey: string
  originalName: string
  mimeType: string
  sizeBytes: number
}

export interface Task {
  id: string
  subjectId: string
  organizationId: string
  createdBy: string
  title: string
  description: string
  deadline: string
  maxScore: number | null
  status: TaskStatus
  attachments: TaskAttachment[]
  createdAt: string
  updatedAt: string | null
}

export interface CreateTaskPayload {
  subjectId: string
  title: string
  description: string
  deadline: string
  maxScore?: number | null
  files?: File[]
}
