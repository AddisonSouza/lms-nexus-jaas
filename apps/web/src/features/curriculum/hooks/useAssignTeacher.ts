import { useMutation, useQueryClient } from '@tanstack/react-query'
import { assignTeacher } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useAssignTeacher(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: Parameters<typeof assignTeacher>[1]) => assignTeacher(subjectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: subjectKeys.detail(subjectId) })
    },
  })
}
