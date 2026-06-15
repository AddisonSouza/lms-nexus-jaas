import { useQuery } from '@tanstack/react-query'
import { getSubject } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useSubject(id: string) {
  return useQuery({
    queryKey: subjectKeys.detail(id),
    queryFn: () => getSubject(id),
    enabled: !!id,
  })
}
