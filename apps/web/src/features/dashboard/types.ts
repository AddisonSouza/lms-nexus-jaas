export type ActivityType =
  | 'CLASSROOM_CREATED'
  | 'CLASSROOM_ARCHIVED'
  | 'TASK_CREATED'
  | 'TASK_EVALUATED'
  | 'MEMBER_JOINED'

export interface ActivityItem {
  type: ActivityType
  referenceId: string
  description: string
  occurredAt: string
}

export interface AdminDashboardData {
  from: string
  to: string
  classroomsByStatus: Record<string, number>
  membersByRole: Record<string, number>
  tasksCreated: number
  tasksEvaluated: number
  averageDeliveryRate: number
  activity: ActivityItem[]
}

export interface DashboardPeriod {
  from: string
  to: string
}

export interface AtRiskStudent {
  studentId: string
  studentName: string
  pendingCount: number
}

export interface ClassroomHealth {
  classroomId: string
  classroomName: string
  status: string
  deliveryRate: number
  averageGrade: number | null
  atRiskStudents: AtRiskStudent[]
}

export interface GestorDashboardData {
  classrooms: ClassroomHealth[]
}
