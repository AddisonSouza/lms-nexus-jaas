import { useQuery } from '@tanstack/react-query'
import { getClassrooms } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useClassrooms() {
  return useQuery({
    queryKey: classroomKeys.lists(),
    queryFn: getClassrooms,
  })
}
