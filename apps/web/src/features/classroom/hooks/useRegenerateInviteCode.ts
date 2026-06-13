import { useMutation, useQueryClient } from '@tanstack/react-query'
import { regenerateInviteCode } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useRegenerateInviteCode(classroomId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => regenerateInviteCode(classroomId),
    onSuccess: (updated) => {
      queryClient.setQueryData(classroomKeys.detail(classroomId), updated)
    },
  })
}
