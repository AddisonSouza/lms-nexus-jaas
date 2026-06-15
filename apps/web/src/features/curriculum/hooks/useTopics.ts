import { useQuery } from '@tanstack/react-query'
import { listTopics } from '../api/topic-api'
import { topicKeys } from '../api/query-keys'

export function useTopics(subjectId: string) {
  return useQuery({
    queryKey: topicKeys.bySubject(subjectId),
    queryFn: () => listTopics(subjectId),
    enabled: !!subjectId,
  })
}
