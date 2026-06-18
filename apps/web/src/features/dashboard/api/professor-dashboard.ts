import { z } from 'zod'
import api from '@lib/axios'

const studentSummarySchema = z.object({
  studentId: z.string(),
  studentName: z.string(),
})

const studentAverageGradeSchema = z.object({
  studentId: z.string(),
  studentName: z.string(),
  averageGrade: z.number(),
})

const professorDashboardSchema = z.object({
  pendingEvaluationsCount: z.number(),
  lastTaskGradeDistribution: z.array(z.number()),
  studentsWithoutSubmission: z.array(studentSummarySchema),
  averageGradePerStudent: z.array(studentAverageGradeSchema),
})

export async function getProfessorDashboard(subjectId: string) {
  const res = await api.get(`/subjects/${subjectId}/dashboard`)
  return professorDashboardSchema.parse(res.data)
}
