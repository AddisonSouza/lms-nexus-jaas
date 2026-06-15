import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateContent, type UpdateContentPayload } from '../api/content-api'
import { contentKeys } from '../api/query-keys'

export function useUpdateContent(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ contentId, payload }: { contentId: string; payload: UpdateContentPayload }) =>
      updateContent(subjectId, contentId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: contentKeys.bySubject(subjectId) })
    },
  })
}
