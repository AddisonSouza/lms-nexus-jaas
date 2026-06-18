import { z } from 'zod'
import api from '@lib/axios'

const atRiskStudentSchema = z.object({
  studentId: z.string(),
  studentName: z.string(),
  pendingCount: z.number(),
})

const classroomHealthSchema = z.object({
  classroomId: z.string(),
  classroomName: z.string(),
  status: z.string(),
  deliveryRate: z.number(),
  averageGrade: z.number().nullable(),
  atRiskStudents: z.array(atRiskStudentSchema),
})

const gestorDashboardSchema = z.object({
  classrooms: z.array(classroomHealthSchema),
})

export async function getGestorDashboard(organizationId: string) {
  const res = await api.get(`/organizations/${organizationId}/gestor-dashboard`)
  return gestorDashboardSchema.parse(res.data)
}

export async function exportGestorDashboardPdf(organizationId: string) {
  const res = await api.get(`/organizations/${organizationId}/gestor-dashboard/pdf`, {
    responseType: 'blob',
  })
  return res.data as Blob
}
