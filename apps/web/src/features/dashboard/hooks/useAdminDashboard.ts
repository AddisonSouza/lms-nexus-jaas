import { useQuery } from '@tanstack/react-query'
import { getAdminDashboard } from '../api/dashboard'
import { dashboardKeys } from '../api/query-keys'
import type { DashboardPeriod } from '../types'

export function useAdminDashboard(organizationId: string, period: DashboardPeriod) {
  return useQuery({
    queryKey: dashboardKeys.admin(organizationId, period),
    queryFn: () => getAdminDashboard(organizationId, period),
  })
}
