import { useMutation, useQueryClient } from '@tanstack/react-query'
import { joinClassroom } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useJoinClassroom() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (inviteCode: string) => joinClassroom(inviteCode),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classroomKeys.lists() })
    },
  })
}
