import { useQuery } from '@tanstack/react-query'
import { getGestorDashboard } from '../api/gestor-dashboard'
import { dashboardKeys } from '../api/query-keys'

export function useGestorDashboard(organizationId: string) {
  return useQuery({
    queryKey: dashboardKeys.gestor(organizationId),
    queryFn: () => getGestorDashboard(organizationId),
  })
}
