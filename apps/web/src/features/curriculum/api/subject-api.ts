import { z } from 'zod'
import api from '@lib/axios'
import type {
  AssignTeacherPayload,
  CreateSubjectPayload,
  LinkClassroomPayload,
  UpdateSubjectPayload,
} from '../types'

const subjectSchema = z.object({
  id: z.string(),
  name: z.string(),
  code: z.string().nullable().default(null),
  description: z.string().nullable().default(null),
  workloadHours: z.number().nullable().default(null),
  organizationId: z.string().default(''),
  classroomIds: z.array(z.string()).default([]),
  teacherMemberIds: z.array(z.string()).default([]),
  createdAt: z.string().default(''),
})

export async function listSubjects() {
  const res = await api.get('/subjects')
  return z.array(subjectSchema).parse(res.data)
}

export async function getSubject(id: string) {
  const res = await api.get(`/subjects/${id}`)
  return subjectSchema.parse(res.data)
}

export async function createSubject(data: CreateSubjectPayload) {
  const res = await api.post('/subjects', data)
  return subjectSchema.parse(res.data)
}

export async function updateSubject(id: string, data: UpdateSubjectPayload) {
  const res = await api.put(`/subjects/${id}`, data)
  return subjectSchema.parse(res.data)
}

export async function deleteSubject(id: string): Promise<void> {
  await api.delete(`/subjects/${id}`)
}

export async function linkClassroom(subjectId: string, data: LinkClassroomPayload): Promise<void> {
  await api.post(`/subjects/${subjectId}/classrooms`, data)
}

export async function unlinkClassroom(subjectId: string, classroomId: string): Promise<void> {
  await api.delete(`/subjects/${subjectId}/classrooms/${classroomId}`)
}

export async function assignTeacher(subjectId: string, data: AssignTeacherPayload): Promise<void> {
  await api.post(`/subjects/${subjectId}/teachers`, data)
}

export async function removeTeacher(subjectId: string, memberId: string): Promise<void> {
  await api.delete(`/subjects/${subjectId}/teachers/${memberId}`)
}
