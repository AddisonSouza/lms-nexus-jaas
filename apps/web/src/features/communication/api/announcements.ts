import { z } from 'zod'
import api from '@lib/axios'
import type { CreateAnnouncementPayload, UpdateAnnouncementPayload } from '../types'

const announcementAttachmentSchema = z.object({
  id: z.string(),
  fileKey: z.string().nullable(),
  originalName: z.string().nullable(),
  mimeType: z.string().nullable(),
  sizeBytes: z.number().nullable(),
  externalUrl: z.string().nullable(),
  linkTitle: z.string().nullable(),
})

const announcementSchema = z.object({
  id: z.string(),
  classroomId: z.string(),
  organizationId: z.string(),
  authorId: z.string(),
  content: z.string(),
  attachments: z.array(announcementAttachmentSchema),
  createdAt: z.string(),
  updatedAt: z.string().nullable(),
})

function buildFormData(data: { content?: string; externalUrl?: string; linkTitle?: string; files?: File[] }) {
  const form = new FormData()
  if (data.content) form.append('content', data.content)
  if (data.externalUrl) form.append('externalUrl', data.externalUrl)
  if (data.linkTitle) form.append('linkTitle', data.linkTitle)
  data.files?.forEach((file) => form.append('files', file))
  return form
}

export async function listAnnouncements(classroomId: string) {
  const res = await api.get(`/classrooms/${classroomId}/announcements`)
  return z.array(announcementSchema).parse(res.data)
}

export async function createAnnouncement(data: CreateAnnouncementPayload) {
  const form = buildFormData(data)
  const res = await api.post(`/classrooms/${data.classroomId}/announcements`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return announcementSchema.parse(res.data)
}

export async function updateAnnouncement(data: UpdateAnnouncementPayload) {
  const form = buildFormData(data)
  const res = await api.put(`/announcements/${data.id}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return announcementSchema.parse(res.data)
}

export async function deleteAnnouncement(id: string) {
  await api.delete(`/announcements/${id}`)
}
