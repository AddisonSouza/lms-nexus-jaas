import { useMutation, useQueryClient } from '@tanstack/react-query'
import { deleteTopic } from '../api/topic-api'
import { topicKeys, contentKeys } from '../api/query-keys'

export function useDeleteTopic(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (topicId: string) => deleteTopic(subjectId, topicId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: topicKeys.bySubject(subjectId) })
      queryClient.invalidateQueries({ queryKey: contentKeys.bySubject(subjectId) })
    },
  })
}
