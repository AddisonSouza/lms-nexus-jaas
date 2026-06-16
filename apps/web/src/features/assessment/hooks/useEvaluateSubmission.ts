import { useMutation, useQueryClient } from '@tanstack/react-query'
import { evaluateSubmission } from '../api/submissions'
import { submissionKeys } from '../api/query-keys'
import type { EvaluateSubmissionPayload } from '../types'

export function useEvaluateSubmission(taskId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ submissionId, payload }: { submissionId: string; payload: EvaluateSubmissionPayload }) =>
      evaluateSubmission(submissionId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: submissionKeys.byTask(taskId) })
    },
  })
}
