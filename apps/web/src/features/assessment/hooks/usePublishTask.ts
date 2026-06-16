import { useMutation, useQueryClient } from '@tanstack/react-query'
import { publishTask } from '../api/tasks'
import { taskKeys } from '../api/query-keys'

export function usePublishTask() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (taskId: string) => publishTask(taskId),
    onSuccess: (_, taskId) => {
      queryClient.invalidateQueries({ queryKey: taskKeys.detail(taskId) })
      queryClient.invalidateQueries({ queryKey: taskKeys.lists() })
    },
  })
}
