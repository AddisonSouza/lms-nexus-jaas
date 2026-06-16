import { useQuery } from '@tanstack/react-query'
import { listSubmissions } from '../api/submissions'
import { submissionKeys } from '../api/query-keys'

export function useSubmissions(taskId: string) {
  return useQuery({
    queryKey: submissionKeys.byTask(taskId),
    queryFn: () => listSubmissions(taskId),
    enabled: !!taskId,
  })
}
