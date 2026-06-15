import api from '@lib/axios'
import type {
  AssignTeacherPayload,
  CreateSubjectPayload,
  LinkClassroomPayload,
  Subject,
  UpdateSubjectPayload,
} from '../types'

export async function listSubjects(): Promise<Subject[]> {
  const res = await api.get<Subject[]>('/subjects')
  return res.data
}

export async function getSubject(id: string): Promise<Subject> {
  const res = await api.get<Subject>(`/subjects/${id}`)
  return res.data
}

export async function createSubject(data: CreateSubjectPayload): Promise<Subject> {
  const res = await api.post<Subject>('/subjects', data)
  return res.data
}

export async function updateSubject(id: string, data: UpdateSubjectPayload): Promise<Subject> {
  const res = await api.put<Subject>(`/subjects/${id}`, data)
  return res.data
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
