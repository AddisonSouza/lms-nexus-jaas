import { useQuery } from '@tanstack/react-query'
import { listSubjects } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useSubjects() {
  return useQuery({
    queryKey: subjectKeys.lists(),
    queryFn: listSubjects,
  })
}
