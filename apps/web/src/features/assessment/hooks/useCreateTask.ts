import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createTask } from '../api/tasks'
import { taskKeys } from '../api/query-keys'

export function useCreateTask() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createTask,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: taskKeys.lists() })
    },
  })
}
