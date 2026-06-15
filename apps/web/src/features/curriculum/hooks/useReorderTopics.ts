import { useMutation, useQueryClient } from '@tanstack/react-query'
import { reorderTopics } from '../api/topic-api'
import { topicKeys } from '../api/query-keys'

export function useReorderTopics(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (topicIds: string[]) => reorderTopics(subjectId, topicIds),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: topicKeys.bySubject(subjectId) })
    },
  })
}
