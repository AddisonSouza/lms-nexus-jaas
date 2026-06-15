import { useMutation, useQueryClient } from '@tanstack/react-query'
import { removeTeacher } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useRemoveTeacher(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (memberId: string) => removeTeacher(subjectId, memberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: subjectKeys.detail(subjectId) })
    },
  })
}
