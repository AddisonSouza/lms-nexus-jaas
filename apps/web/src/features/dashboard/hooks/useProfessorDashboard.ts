import { useQuery } from '@tanstack/react-query'
import { isAxiosError } from 'axios'
import { getProfessorDashboard } from '../api/professor-dashboard'
import { dashboardKeys } from '../api/query-keys'

export function useProfessorDashboard(subjectId: string) {
  return useQuery({
    queryKey: dashboardKeys.professor(subjectId),
    queryFn: () => getProfessorDashboard(subjectId),
    // 403 = não leciona nesta disciplina: parar o polling em vez de repetir a cada 30s
    refetchInterval: (query) => {
      const { error } = query.state
      return isAxiosError(error) && error.response?.status === 403 ? false : 30_000
    },
  })
}
