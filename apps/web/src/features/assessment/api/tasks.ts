import { z } from 'zod'
import api from '@lib/axios'
import type { CreateTaskPayload } from '../types'

const taskAttachmentSchema = z.object({
  id: z.string(),
  fileKey: z.string(),
  originalName: z.string(),
  mimeType: z.string(),
  sizeBytes: z.number(),
})

const taskSchema = z.object({
  id: z.string(),
  subjectId: z.string(),
  organizationId: z.string(),
  createdBy: z.string(),
  title: z.string(),
  description: z.string(),
  deadline: z.string(),
  maxScore: z.number().nullable(),
  status: z.enum(['DRAFT', 'PUBLISHED', 'CLOSED', 'GRADED']),
  attachments: z.array(taskAttachmentSchema),
  createdAt: z.string(),
  updatedAt: z.string().nullable(),
})

export async function listTasks() {
  const res = await api.get('/tasks')
  return z.array(taskSchema).parse(res.data)
}

export async function createTask(data: CreateTaskPayload) {
  const form = new FormData()
  form.append('subjectId', data.subjectId)
  form.append('title', data.title)
  form.append('description', data.description)
  form.append('deadline', data.deadline)
  if (data.maxScore != null) form.append('maxScore', String(data.maxScore))
  if (data.files) {
    data.files.forEach((file) => form.append('files', file))
  }
  const res = await api.post('/tasks', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return taskSchema.parse(res.data)
}

export async function publishTask(taskId: string) {
  const res = await api.patch(`/tasks/${taskId}/publish`)
  return taskSchema.parse(res.data)
}
