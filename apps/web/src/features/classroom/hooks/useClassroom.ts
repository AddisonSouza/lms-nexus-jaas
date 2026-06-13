import { useQuery } from '@tanstack/react-query'
import { getClassroom } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useClassroom(id: string) {
  return useQuery({
    queryKey: classroomKeys.detail(id),
    queryFn: () => getClassroom(id),
    enabled: !!id,
  })
}
