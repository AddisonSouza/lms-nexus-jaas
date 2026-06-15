import { useMutation, useQueryClient } from '@tanstack/react-query'
import { deleteContent } from '../api/content-api'
import { contentKeys } from '../api/query-keys'

export function useDeleteContent(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (contentId: string) => deleteContent(subjectId, contentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: contentKeys.bySubject(subjectId) })
    },
  })
}
