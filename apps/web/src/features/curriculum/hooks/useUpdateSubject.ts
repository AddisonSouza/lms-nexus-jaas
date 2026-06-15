import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateSubject } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useUpdateSubject(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: Parameters<typeof updateSubject>[1]) => updateSubject(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: subjectKeys.lists() })
      queryClient.invalidateQueries({ queryKey: subjectKeys.detail(id) })
    },
  })
}
