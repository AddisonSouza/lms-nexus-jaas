import { useQuery } from '@tanstack/react-query'
import { listSubjectContents } from '../api/content-api'
import { contentKeys } from '../api/query-keys'

export function useSubjectContents(subjectId: string) {
  return useQuery({
    queryKey: contentKeys.bySubject(subjectId),
    queryFn: () => listSubjectContents(subjectId),
    enabled: !!subjectId,
  })
}
