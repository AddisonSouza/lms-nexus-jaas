import { useMutation, useQueryClient } from '@tanstack/react-query'
import { deleteClassroom } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useDeleteClassroom() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: deleteClassroom,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classroomKeys.lists() })
    },
  })
}
