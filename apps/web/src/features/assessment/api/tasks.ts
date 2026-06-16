import api from '@lib/axios'
import type { CreateTaskPayload, Task } from '../types'

export async function createTask(data: CreateTaskPayload): Promise<Task> {
  const form = new FormData()
  form.append('subjectId', data.subjectId)
  form.append('title', data.title)
  form.append('description', data.description)
  form.append('deadline', data.deadline)
  if (data.maxScore != null) form.append('maxScore', String(data.maxScore))
  if (data.files) {
    data.files.forEach((file) => form.append('files', file))
  }
  const res = await api.post<Task>('/tasks', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data
}

export async function publishTask(taskId: string): Promise<Task> {
  const res = await api.patch<Task>(`/tasks/${taskId}/publish`)
  return res.data
}
