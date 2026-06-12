import api from '@lib/axios'
import type {
  AddMemberPayload,
  Classroom,
  ClassroomMember,
  CreateClassroomPayload,
  UpdateClassroomPayload,
} from '../types'

export async function getClassrooms(): Promise<Classroom[]> {
  const res = await api.get<Classroom[]>('/classrooms')
  return res.data
}

export async function getClassroom(id: string): Promise<Classroom> {
  const res = await api.get<Classroom>(`/classrooms/${id}`)
  return res.data
}

export async function createClassroom(data: CreateClassroomPayload): Promise<Classroom> {
  const res = await api.post<Classroom>('/classrooms', data)
  return res.data
}

export async function updateClassroom(id: string, data: UpdateClassroomPayload): Promise<Classroom> {
  const res = await api.put<Classroom>(`/classrooms/${id}`, data)
  return res.data
}

export async function deleteClassroom(id: string): Promise<void> {
  await api.delete(`/classrooms/${id}`)
}

export async function getClassroomMembers(id: string): Promise<ClassroomMember[]> {
  const res = await api.get<ClassroomMember[]>(`/classrooms/${id}/members`)
  return res.data
}

export async function addClassroomMember(classroomId: string, data: AddMemberPayload): Promise<ClassroomMember> {
  const res = await api.post<ClassroomMember>(`/classrooms/${classroomId}/members`, data)
  return res.data
}

export async function removeClassroomMember(classroomId: string, userId: string): Promise<void> {
  await api.delete(`/classrooms/${classroomId}/members/${userId}`)
}
