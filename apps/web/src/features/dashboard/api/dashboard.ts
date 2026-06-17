import { z } from 'zod'
import api from '@lib/axios'
import type { DashboardPeriod } from '../types'

const activityItemSchema = z.object({
  type: z.enum(['CLASSROOM_CREATED', 'CLASSROOM_ARCHIVED', 'TASK_CREATED', 'TASK_EVALUATED', 'MEMBER_JOINED']),
  referenceId: z.string(),
  description: z.string(),
  occurredAt: z.string(),
})

const adminDashboardSchema = z.object({
  from: z.string(),
  to: z.string(),
  classroomsByStatus: z.record(z.number()),
  membersByRole: z.record(z.number()),
  tasksCreated: z.number(),
  tasksEvaluated: z.number(),
  averageDeliveryRate: z.number(),
  activity: z.array(activityItemSchema),
})

export async function getAdminDashboard(organizationId: string, period: DashboardPeriod) {
  const res = await api.get(`/organizations/${organizationId}/dashboard`, {
    params: { from: period.from, to: period.to },
  })
  return adminDashboardSchema.parse(res.data)
}

export async function exportAdminDashboardPdf(organizationId: string, period: DashboardPeriod) {
  const res = await api.get(`/organizations/${organizationId}/reports/pdf`, {
    params: { from: period.from, to: period.to },
    responseType: 'blob',
  })
  return res.data as Blob
}
