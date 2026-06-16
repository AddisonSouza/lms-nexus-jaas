import api from '@lib/axios'
import type { EditSubmissionPayload, SubmitTaskPayload, Task, TaskSubmission } from '../types'

export async function listPublishedTasks(): Promise<Task[]> {
  const res = await api.get<Task[]>('/tasks/published')
  return res.data
}

export async function createSubmission(data: SubmitTaskPayload): Promise<TaskSubmission> {
  const form = new FormData()
  if (data.textResponse) form.append('textResponse', data.textResponse)
  data.files?.forEach((file) => form.append('files', file))
  const res = await api.post<TaskSubmission>(`/tasks/${data.taskId}/submissions`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data
}

export async function updateSubmission(data: EditSubmissionPayload): Promise<TaskSubmission> {
  const form = new FormData()
  if (data.textResponse) form.append('textResponse', data.textResponse)
  data.files?.forEach((file) => form.append('files', file))
  const res = await api.put<TaskSubmission>(
    `/tasks/${data.taskId}/submissions/${data.submissionId}`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  return res.data
}
