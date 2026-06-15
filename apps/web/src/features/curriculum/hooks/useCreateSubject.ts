import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createSubject } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useCreateSubject() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createSubject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: subjectKeys.lists() })
    },
  })
}
