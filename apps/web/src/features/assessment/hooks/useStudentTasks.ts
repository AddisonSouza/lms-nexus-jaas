import { useQuery } from '@tanstack/react-query'
import { listPublishedTasks } from '../api/submissions'
import { taskKeys } from '../api/query-keys'

export function useStudentTasks() {
  return useQuery({
    queryKey: taskKeys.published(),
    queryFn: listPublishedTasks,
  })
}
