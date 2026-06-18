import type { DashboardPeriod } from '../types'

export const dashboardKeys = {
  all: ['dashboard'] as const,
  admin: (organizationId: string, period: DashboardPeriod) =>
    [...dashboardKeys.all, 'admin', organizationId, period] as const,
  gestor: (organizationId: string) =>
    [...dashboardKeys.all, 'gestor', organizationId] as const,
  professor: (subjectId: string) =>
    [...dashboardKeys.all, 'professor', subjectId] as const,
}
