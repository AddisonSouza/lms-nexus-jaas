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

export type ContentType = 'VIDEO' | 'DOCUMENTO' | 'LINK' | 'ARQUIVO'

export interface Topic {
  id: string
  subjectId: string
  organizationId: string
  title: string
  position: number
  createdAt: string
  updatedAt: string | null
}

export interface SubjectContent {
  id: string
  topicId: string
  organizationId: string
  title: string
  contentType: ContentType
  externalUrl: string | null
  fileKey: string | null
  description: string | null
  position: number
  createdAt: string
  updatedAt: string | null
}

export interface TopicWithContents {
  topic: Topic
  contents: SubjectContent[]
}

export interface SubjectContentsGrouped {
  topics: TopicWithContents[]
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
