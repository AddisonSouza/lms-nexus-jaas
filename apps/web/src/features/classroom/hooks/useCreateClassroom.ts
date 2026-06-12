import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createClassroom } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useCreateClassroom() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createClassroom,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classroomKeys.lists() })
    },
  })
}
