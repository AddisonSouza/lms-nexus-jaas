import { useMutation, useQueryClient } from '@tanstack/react-query'
import { updateClassroom } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useUpdateClassroom(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: Parameters<typeof updateClassroom>[1]) => updateClassroom(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classroomKeys.lists() })
      queryClient.invalidateQueries({ queryKey: classroomKeys.detail(id) })
    },
  })
}
