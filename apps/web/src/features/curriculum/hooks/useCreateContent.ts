import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createContent, type CreateContentPayload } from '../api/content-api'
import { contentKeys } from '../api/query-keys'

export function useCreateContent(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateContentPayload) => createContent(subjectId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: contentKeys.bySubject(subjectId) })
    },
  })
}
