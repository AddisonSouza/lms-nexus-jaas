import type { DashboardPeriod } from '../types'

export const dashboardKeys = {
  all: ['dashboard'] as const,
  admin: (organizationId: string, period: DashboardPeriod) =>
    [...dashboardKeys.all, 'admin', organizationId, period] as const,
}
