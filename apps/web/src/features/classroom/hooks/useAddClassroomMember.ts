import { useMutation, useQueryClient } from '@tanstack/react-query'
import { addClassroomMember } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'
import type { AddMemberPayload } from '../types'

export function useAddClassroomMember(classroomId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (data: AddMemberPayload) => addClassroomMember(classroomId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classroomKeys.members(classroomId) })
    },
  })
}
