import { useMutation, useQueryClient } from '@tanstack/react-query'
import { removeClassroomMember } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useRemoveClassroomMember(classroomId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (userId: string) => removeClassroomMember(classroomId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classroomKeys.members(classroomId) })
    },
  })
}
