import { useMutation, useQueryClient } from '@tanstack/react-query'
import { linkClassroom } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useLinkClassroom(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: Parameters<typeof linkClassroom>[1]) => linkClassroom(subjectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: subjectKeys.detail(subjectId) })
    },
  })
}
