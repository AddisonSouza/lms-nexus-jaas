import api from '@lib/axios'
import type { ContentType, SubjectContent, SubjectContentsGrouped } from '../types'

export async function listSubjectContents(subjectId: string): Promise<SubjectContentsGrouped> {
  const res = await api.get<SubjectContentsGrouped>(`/subjects/${subjectId}/contents`)
  return res.data
}

export interface CreateContentPayload {
  topicId: string
  title: string
  contentType: ContentType
  externalUrl?: string
  description?: string
  file?: File
}

export async function createContent(subjectId: string, payload: CreateContentPayload): Promise<SubjectContent> {
  const formData = new FormData()
  formData.append('topicId', payload.topicId)
  formData.append('title', payload.title)
  formData.append('contentType', payload.contentType)
  if (payload.externalUrl) formData.append('externalUrl', payload.externalUrl)
  if (payload.description) formData.append('description', payload.description)
  if (payload.file) formData.append('file', payload.file)

  const res = await api.post<SubjectContent>(`/subjects/${subjectId}/contents`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data
}

export interface UpdateContentPayload {
  title?: string
  description?: string
  externalUrl?: string
}

export async function updateContent(
  subjectId: string,
  contentId: string,
  payload: UpdateContentPayload,
): Promise<SubjectContent> {
  const res = await api.put<SubjectContent>(`/subjects/${subjectId}/contents/${contentId}`, payload)
  return res.data
}

export async function deleteContent(subjectId: string, contentId: string): Promise<void> {
  await api.delete(`/subjects/${subjectId}/contents/${contentId}`)
}
