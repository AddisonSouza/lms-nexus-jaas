import { z } from 'zod'
import api from '@lib/axios'
import type { EditSubmissionPayload, EvaluateSubmissionPayload, SubmitTaskPayload } from '../types'

const submissionAttachmentSchema = z.object({
  id: z.string(),
  fileKey: z.string(),
  originalName: z.string(),
  mimeType: z.string(),
  sizeBytes: z.number(),
})

const submissionSchema = z.object({
  id: z.string(),
  taskId: z.string(),
  studentId: z.string(),
  organizationId: z.string(),
  textResponse: z.string().nullable(),
  status: z.enum(['SUBMITTED', 'LATE', 'EVALUATED']),
  grade: z.number().nullable(),
  feedback: z.string().nullable(),
  attachments: z.array(submissionAttachmentSchema),
  createdAt: z.string(),
  updatedAt: z.string().nullable(),
})

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

const submissionSummarySchema = z.object({
  id: z.string(),
  status: z.enum(['SUBMITTED', 'LATE', 'EVALUATED']),
  grade: z.number().nullable(),
  feedback: z.string().nullable(),
  submittedAt: z.string(),
  lateSubmission: z.boolean(),
})

const taskWithGradeSchema = taskSchema.extend({
  submission: submissionSummarySchema.nullable(),
})

export async function listPublishedTasks() {
  const res = await api.get('/tasks/published')
  return z.array(taskSchema).parse(res.data)
}

export async function createSubmission(data: SubmitTaskPayload) {
  const form = new FormData()
  if (data.textResponse) form.append('textResponse', data.textResponse)
  data.files?.forEach((file) => form.append('files', file))
  const res = await api.post(`/tasks/${data.taskId}/submissions`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return submissionSchema.parse(res.data)
}

export async function updateSubmission(data: EditSubmissionPayload) {
  const form = new FormData()
  if (data.textResponse) form.append('textResponse', data.textResponse)
  data.files?.forEach((file) => form.append('files', file))
  const res = await api.put(
    `/tasks/${data.taskId}/submissions/${data.submissionId}`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  return submissionSchema.parse(res.data)
}

export async function listSubmissions(taskId: string) {
  const res = await api.get(`/tasks/${taskId}/submissions`)
  return z.array(submissionSchema).parse(res.data)
}

export async function evaluateSubmission(
  submissionId: string,
  payload: EvaluateSubmissionPayload,
) {
  const res = await api.patch(`/submissions/${submissionId}/evaluation`, payload)
  return submissionSchema.parse(res.data)
}

export async function listStudentGrades() {
  const res = await api.get('/tasks/my-grades')
  return z.array(taskWithGradeSchema).parse(res.data)
}
