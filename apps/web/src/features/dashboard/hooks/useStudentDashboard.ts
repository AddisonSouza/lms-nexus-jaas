import { useQuery } from '@tanstack/react-query'
import { getStudentDashboard } from '../api/student-dashboard'
import { dashboardKeys } from '../api/query-keys'

export function useStudentDashboard() {
  return useQuery({
    queryKey: dashboardKeys.student(),
    queryFn: () => getStudentDashboard(),
    refetchInterval: 30_000,
  })
}
