import { z } from 'zod'
import api from '@lib/axios'

const upcomingTaskSchema = z.object({
  taskId: z.string(),
  title: z.string(),
  subjectName: z.string(),
  deadline: z.string(),
})

const recentGradeSchema = z.object({
  taskId: z.string(),
  title: z.string(),
  subjectName: z.string(),
  grade: z.number(),
  feedback: z.string().nullable(),
})

const subjectAverageGradeSchema = z.object({
  subjectId: z.string(),
  subjectName: z.string(),
  averageGrade: z.number(),
})

const studentDashboardSchema = z.object({
  upcomingPendingTasks: z.array(upcomingTaskSchema),
  pendingTasksCount: z.number(),
  submittedTasksCount: z.number(),
  recentGrades: z.array(recentGradeSchema),
  averageGradePerSubject: z.array(subjectAverageGradeSchema),
})

export async function getStudentDashboard() {
  const res = await api.get('/students/me/dashboard')
  return studentDashboardSchema.parse(res.data)
}
