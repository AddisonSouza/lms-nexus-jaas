import { z } from 'zod'
import api from '@lib/axios'
import type {
  AddMemberPayload,
  CreateClassroomPayload,
  UpdateClassroomPayload,
} from '../types'

const classroomSchema = z.object({
  id: z.string(),
  name: z.string(),
  description: z.string().nullable(),
  academicPeriod: z.string(),
  status: z.enum(['ACTIVE', 'ARCHIVED']),
  inviteCode: z.string().nullable(),
  organizationId: z.string(),
  createdAt: z.string(),
})

const classroomMemberSchema = z.object({
  id: z.string(),
  classroomId: z.string(),
  userId: z.string(),
  userName: z.string().nullish().transform((v) => v ?? undefined),
  role: z.enum(['PROFESSOR', 'ALUNO']),
  joinedAt: z.string(),
})

export async function getClassrooms() {
  const res = await api.get('/classrooms')
  return z.array(classroomSchema).parse(res.data)
}

export async function getClassroom(id: string) {
  const res = await api.get(`/classrooms/${id}`)
  return classroomSchema.parse(res.data)
}

export async function createClassroom(data: CreateClassroomPayload) {
  const res = await api.post('/classrooms', data)
  return classroomSchema.parse(res.data)
}

export async function updateClassroom(id: string, data: UpdateClassroomPayload) {
  const res = await api.put(`/classrooms/${id}`, data)
  return classroomSchema.parse(res.data)
}

export async function deleteClassroom(id: string): Promise<void> {
  await api.delete(`/classrooms/${id}`)
}

export async function getClassroomMembers(id: string) {
  const res = await api.get(`/classrooms/${id}/members`)
  return z.array(classroomMemberSchema).parse(res.data)
}

export async function addClassroomMember(classroomId: string, data: AddMemberPayload) {
  const res = await api.post(`/classrooms/${classroomId}/members`, data)
  return classroomMemberSchema.parse(res.data)
}

export async function removeClassroomMember(classroomId: string, userId: string): Promise<void> {
  await api.delete(`/classrooms/${classroomId}/members/${userId}`)
}

export async function joinClassroom(inviteCode: string) {
  const res = await api.post('/classrooms/join', { inviteCode })
  return classroomSchema.parse(res.data)
}
