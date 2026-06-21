export type ClassroomStatus = 'ACTIVE' | 'ARCHIVED'

export type ClassroomMemberRole = 'PROFESSOR' | 'ALUNO'

export interface Classroom {
  id: string
  name: string
  description: string | null
  academicPeriod: string
  status: ClassroomStatus
  inviteCode: string | null
  organizationId: string
  createdAt: string
}

export interface ClassroomMember {
  id: string
  classroomId: string
  userId: string
  userName?: string
  role: ClassroomMemberRole
  joinedAt: string
}

export interface CreateClassroomPayload {
  name: string
  description?: string
  academicPeriod: string
}

export interface UpdateClassroomPayload {
  name?: string
  description?: string
  academicPeriod?: string
  status?: ClassroomStatus
}

export interface AddMemberPayload {
  userId: string
  role: ClassroomMemberRole
}
