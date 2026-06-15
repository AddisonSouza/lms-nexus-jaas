import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateTopic } from '../api/topic-api'
import { topicKeys, contentKeys } from '../api/query-keys'

export function useUpdateTopic(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ topicId, title }: { topicId: string; title: string }) =>
      updateTopic(subjectId, topicId, title),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: topicKeys.bySubject(subjectId) })
      queryClient.invalidateQueries({ queryKey: contentKeys.bySubject(subjectId) })
    },
  })
}
