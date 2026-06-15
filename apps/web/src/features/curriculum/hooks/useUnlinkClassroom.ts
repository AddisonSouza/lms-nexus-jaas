import { useMutation, useQueryClient } from '@tanstack/react-query'
import { unlinkClassroom } from '../api/subject-api'
import { subjectKeys } from '../api/query-keys'

export function useUnlinkClassroom(subjectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (classroomId: string) => unlinkClassroom(subjectId, classroomId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: subjectKeys.detail(subjectId) })
    },
  })
}
