import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createTopic } from '../api/topic-api'
import { topicKeys, contentKeys } from '../api/query-keys'

export function useCreateTopic(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (title: string) => createTopic(subjectId, title),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: topicKeys.bySubject(subjectId) })
      queryClient.invalidateQueries({ queryKey: contentKeys.bySubject(subjectId) })
    },
  })
}
