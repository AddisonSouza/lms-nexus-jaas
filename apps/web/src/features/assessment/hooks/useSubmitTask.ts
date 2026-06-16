import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createSubmission, updateSubmission } from '../api/submissions'
import { submissionKeys } from '../api/query-keys'

export function useSubmitTask(taskId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createSubmission,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: submissionKeys.byTask(taskId) })
    },
  })
}

export function useEditSubmission(taskId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: updateSubmission,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: submissionKeys.byTask(taskId) })
    },
  })
}
