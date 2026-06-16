export type TaskStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'GRADED'
export type SubmissionStatus = 'SUBMITTED' | 'EVALUATED'

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

export interface SubmissionAttachment {
  id: string
  fileKey: string
  originalName: string
  mimeType: string
  sizeBytes: number
}

export interface TaskSubmission {
  id: string
  taskId: string
  studentId: string
  organizationId: string
  textResponse: string | null
  status: SubmissionStatus
  attachments: SubmissionAttachment[]
  createdAt: string
  updatedAt: string | null
}

export interface SubmitTaskPayload {
  taskId: string
  textResponse?: string
  files?: File[]
}

export interface EditSubmissionPayload {
  taskId: string
  submissionId: string
  textResponse?: string
  files?: File[]
}
