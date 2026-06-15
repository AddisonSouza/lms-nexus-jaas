import { useMutation, useQueryClient } from '@tanstack/react-query'
import { deleteSubject } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useDeleteSubject() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: deleteSubject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: subjectKeys.lists() })
    },
  })
}
