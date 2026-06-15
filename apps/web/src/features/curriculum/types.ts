export interface Subject {
  id: string
  name: string
  code: string | null
  description: string | null
  workloadHours: number | null
  organizationId: string
  classroomIds: string[]
  teacherMemberIds: string[]
  createdAt: string
}

export interface CreateSubjectPayload {
  name: string
  code?: string
  description?: string
  workloadHours?: number
}

export interface UpdateSubjectPayload {
  name?: string
  code?: string
  description?: string
  workloadHours?: number
}

export interface LinkClassroomPayload {
  classroomId: string
}

export interface AssignTeacherPayload {
  memberId: string
}
