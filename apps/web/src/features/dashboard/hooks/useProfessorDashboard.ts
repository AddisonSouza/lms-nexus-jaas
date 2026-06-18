import { useQuery } from '@tanstack/react-query'
import { getProfessorDashboard } from '../api/professor-dashboard'
import { dashboardKeys } from '../api/query-keys'

export function useProfessorDashboard(subjectId: string) {
  return useQuery({
    queryKey: dashboardKeys.professor(subjectId),
    queryFn: () => getProfessorDashboard(subjectId),
    refetchInterval: 30_000,
  })
}
